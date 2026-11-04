package com.medical.cardio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import com.medical.cardio.service.RefreshTokenService
import com.medical.cardio.support.ControllerTestBase
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post as postRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.net.HttpCookie
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.HexFormat

class RefreshControllerTest : ControllerTestBase() {
    @Autowired private lateinit var adminUserRepository: AdminUserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var refreshTokenService: RefreshTokenService
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Value("\${app.jwt.secret}") private lateinit var jwtSecret: String

    private lateinit var user: AdminUserEntity

    @BeforeEach
    fun setUp() {
        user = adminUserRepository.save(AdminUserEntity(
            email = "refresh-controller@cardio.test",
            passwordHash = passwordEncoder.encode("TestPassword@2000")
        ))
    }

    @AfterEach
    fun tearDown() {
        if (adminUserRepository.existsById(user.id)) adminUserRepository.deleteById(user.id)
    }

    @ParameterizedTest
    @CsvSource("false,missing", "true,missing", "false,expired", "true,expired", "false,invalid", "true,invalid")
    fun `refresh issues usable access and preserves session and cookie policy`(rememberMe: Boolean, accessState: String) {
        val loginCookies = login(rememberMe)
        val rawRefreshToken = loginCookies.getValue("refresh_token").value
        val sessionBefore = requireNotNull(refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken)))
        val sessionCount = refreshTokenRepository.count()
        val staleAccess = when (accessState) {
            "expired" -> expiredAccess()
            "invalid" -> "invalid-access-token"
            else -> null
        }
        if (accessState == "expired") {
            mockMvc.get("/api/auth/me") { cookie(Cookie("access_token", staleAccess!!)) }
                .andExpect { status { isUnauthorized() }; jsonPath("$.code") { value("TOKEN_EXPIRED") } }
        }

        val csrf = csrf(staleAccess)
        val response = mockMvc.perform(refreshRequest(rawRefreshToken, csrf, staleAccess))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(user.email))
            .andExpect(jsonPath("$.role").value("ROLE_DOCTOR"))
            .andExpect(jsonPath("$.accessToken").doesNotExist())
            .andExpect(jsonPath("$.refreshToken").doesNotExist())
            .andReturn().response

        val cookies = responseCookies(response)
        assertEquals(setOf("access_token"), cookies.keys)
        val newAccessCookie = cookies.getValue("access_token")
        assertEquals(if (rememberMe) 900L else -1L, newAccessCookie.maxAge)
        assertEquals("/", newAccessCookie.path)
        assertTrue(newAccessCookie.isHttpOnly)
        assertTrue(response.getHeaders("Set-Cookie").single().contains("SameSite=Strict"))
        val lifetimeMs = jwtService.extractClaim(newAccessCookie.value) { it.expiration.time - it.issuedAt.time }
        assertEquals(900_000L, lifetimeMs)

        mockMvc.get("/api/auth/me") { cookie(Cookie("access_token", newAccessCookie.value)) }
            .andExpect { status { isOk() }; jsonPath("$.email") { value(user.email) } }

        val sessionAfter = requireNotNull(refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken)))
        assertEquals(sessionBefore.id, sessionAfter.id)
        assertEquals(sessionBefore.expiresDate, sessionAfter.expiresDate)
        assertEquals(rememberMe, sessionAfter.rememberMe)
        assertNull(sessionAfter.revokedDate)
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @Test
    fun `csrf bootstrap is public stateless and not cacheable`() {
        val sessionCount = refreshTokenRepository.count()
        val result = mockMvc.get("/api/auth/csrf") {
            cookie(Cookie("access_token", "broken-access"))
        }.andExpect {
            status { isOk() }
            header { string("Cache-Control", containsString("no-store")) }
            jsonPath("$.headerName") { value("X-XSRF-TOKEN") }
            jsonPath("$.token") { isNotEmpty() }
        }.andReturn()
        val response = result.response

        val cookie = responseCookies(response).getValue("XSRF-TOKEN")
        assertEquals("/api/auth", cookie.path)
        assertTrue(cookie.isHttpOnly)
        assertFalse(cookie.secure)
        assertEquals("Strict", response.getCookie("XSRF-TOKEN")!!.getAttribute("SameSite"))
        assertNull(result.request.getSession(false))
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "empty", "unknown", "malformed"])
    fun `invalid refresh is a uniform unauthorized problem`(tokenState: String) {
        val rawToken = when (tokenState) {
            "missing" -> null
            "empty" -> ""
            "unknown" -> "a".repeat(43)
            else -> "malformed-refresh-token"
        }
        assertRefreshRejected(rawToken)
    }

    @ParameterizedTest
    @ValueSource(strings = ["expired", "revoked", "blocked", "deleted"])
    fun `unusable session cannot renew access`(state: String) {
        val issued = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val session = requireNotNull(refreshTokenRepository.findByTokenHash(sha256(issued.rawToken)))
        when (state) {
            "expired" -> { session.expiresDate = Instant.now().minusSeconds(1); refreshTokenRepository.save(session) }
            "revoked" -> { session.revokedDate = Instant.now(); refreshTokenRepository.save(session) }
            "blocked" -> { user.isBlocked = true; adminUserRepository.save(user) }
            "deleted" -> adminUserRepository.deleteById(user.id)
        }
        assertRefreshRejected(issued.rawToken)
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "missing-header", "missing-cookie", "wrong", "mismatched"])
    fun `csrf failures reject refresh before access is issued`(state: String) {
        val rawToken = login(false).getValue("refresh_token").value
        val token = csrf()
        val request = postRequest("/api/auth/refresh").cookie(Cookie("refresh_token", rawToken))
        when (state) {
            "missing-header" -> request.cookie(token.cookie)
            "missing-cookie" -> request.header(token.headerName, token.token)
            "wrong" -> request.cookie(token.cookie).header(token.headerName, "invalid-csrf")
            "mismatched" -> request.cookie(token.cookie).header(token.headerName, csrf().token)
        }
        val response = mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"))
            .andExpect(jsonPath("$.instance").value("/api/auth/refresh"))
            .andReturn().response
        assertTrue(response.getHeaders("Set-Cookie").none {
            it.startsWith("access_token=") || it.startsWith("refresh_token=")
        })
    }

    @Test
    fun `refresh uses current user role`() {
        val rawToken = login(false).getValue("refresh_token").value
        adminUserRepository.save(AdminUserEntity(
            id = user.id, email = user.email, passwordHash = user.passwordHash, role = Role.SUPER_ADMIN
        ))

        val response = mockMvc.perform(refreshRequest(rawToken, csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ROLE_SUPER_ADMIN"))
            .andReturn().response
        val access = responseCookies(response).getValue("access_token").value
        assertEquals(listOf("ROLE_SUPER_ADMIN"), jwtService.extractRoles(access))
    }

    @Test
    fun `valid refresh can be used again without extending its expiry`() {
        val rawToken = login(false).getValue("refresh_token").value
        val csrf = csrf()
        val before = requireNotNull(refreshTokenRepository.findByTokenHash(sha256(rawToken)))

        repeat(2) {
            mockMvc.perform(refreshRequest(rawToken, csrf)).andExpect(status().isOk())
        }

        val after = requireNotNull(refreshTokenRepository.findByTokenHash(sha256(rawToken)))
        assertEquals(before.id, after.id)
        assertEquals(before.expiresDate, after.expiresDate)
        assertNull(after.revokedDate)
    }

    @Test
    fun `access cookie alone cannot refresh a session`() {
        val accessToken = login(false).getValue("access_token").value
        mockMvc.perform(refreshRequest(null, csrf(), accessToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"))
    }

    private fun assertRefreshRejected(rawToken: String?) {
        val sessionCount = refreshTokenRepository.count()
        mockMvc.perform(refreshRequest(rawToken, csrf()))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.instance").value("/api/auth/refresh"))
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andExpect(header().doesNotExist("Set-Cookie"))
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    private data class CsrfState(val cookie: Cookie, val headerName: String, val token: String)

    private fun csrf(staleAccess: String? = null): CsrfState {
        val response = mockMvc.get("/api/auth/csrf") {
            if (staleAccess != null) cookie(Cookie("access_token", staleAccess))
        }.andExpect { status { isOk() } }.andReturn().response
        val body = objectMapper.readTree(response.contentAsString)
        val cookie = responseCookies(response).getValue("XSRF-TOKEN")
        return CsrfState(Cookie(cookie.name, cookie.value), body.path("headerName").asText(), body.path("token").asText())
    }

    private fun refreshRequest(rawToken: String?, csrf: CsrfState, accessToken: String? = null): MockHttpServletRequestBuilder {
        val request = postRequest("/api/auth/refresh").cookie(csrf.cookie).header(csrf.headerName, csrf.token)
        if (rawToken != null) request.cookie(Cookie("refresh_token", rawToken))
        if (accessToken != null) request.cookie(Cookie("access_token", accessToken))
        return request
    }

    private fun login(rememberMe: Boolean): Map<String, HttpCookie> {
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"${user.email}","password":"TestPassword@2000","rememberMe":$rememberMe}"""
        }.andExpect { status { isOk() } }.andReturn().response
        return responseCookies(response)
    }

    private fun responseCookies(response: MockHttpServletResponse): Map<String, HttpCookie> =
        response.getHeaders("Set-Cookie").associate { header ->
            val cookie = HttpCookie.parse(header).single()
            cookie.name to cookie
        }

    private fun expiredAccess(): String = Jwts.builder()
        .subject(user.email)
        .issuedAt(Date(System.currentTimeMillis() - 3_600_000))
        .expiration(Date(System.currentTimeMillis() - 60_000))
        .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret)))
        .compact()

    private fun sha256(rawToken: String): String = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
    )
}
