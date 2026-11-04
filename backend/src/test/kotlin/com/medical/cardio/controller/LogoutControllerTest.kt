package com.medical.cardio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import com.medical.cardio.service.RefreshTokenService
import com.medical.cardio.support.ControllerTestBase
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
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

class LogoutControllerTest : ControllerTestBase() {
    @Autowired private lateinit var users: AdminUserRepository
    @Autowired private lateinit var tokens: RefreshTokenRepository
    @Autowired private lateinit var refreshTokenService: RefreshTokenService
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var objectMapper: ObjectMapper
    @Value("\${app.jwt.secret}") private lateinit var jwtSecret: String

    private lateinit var user: AdminUserEntity
    private var otherUser: AdminUserEntity? = null

    @BeforeEach
    fun setUp() {
        user = users.save(AdminUserEntity(
            email = "logout-test@cardio.test",
            passwordHash = passwordEncoder.encode("TestPassword@2000")
        ))
    }

    @AfterEach
    fun tearDown() {
        otherUser?.let { users.deleteById(it.id) }
        users.deleteById(user.id)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `logout revokes current session clears cookies and can be repeated`(rememberMe: Boolean) {
        val loginCookies = login(rememberMe)
        val rawToken = loginCookies.getValue("refresh_token").value
        val sessionBefore = tokens.findByTokenHash(sha256(rawToken))!!
        val count = tokens.count()
        val csrf = csrf()
        val before = Instant.now().minusMillis(1)

        val response = mockMvc.perform(logoutRequest(rawToken, csrf))
            .andExpect(status().isOk())
            .andExpect(content().string(""))
            .andReturn().response

        val after = Instant.now().plusMillis(1)
        assertClearedCookies(response)
        val sessionAfter = tokens.findByTokenHash(sha256(rawToken))!!
        val firstRevocation = requireNotNull(sessionAfter.revokedDate)
        assertTrue(firstRevocation >= before && firstRevocation <= after)
        assertEquals(sessionBefore.id, sessionAfter.id)
        assertEquals(sessionBefore.expiresDate, sessionAfter.expiresDate)
        assertEquals(count, tokens.count())

        val repeated = mockMvc.perform(logoutRequest(rawToken, csrf))
            .andExpect(status().isOk()).andReturn().response
        assertClearedCookies(repeated)
        assertEquals(firstRevocation, tokens.findByTokenHash(sha256(rawToken))!!.revokedDate)
        mockMvc.perform(authRequest("/api/auth/refresh", rawToken, csrf))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "empty", "blank", "unknown"])
    fun `logout without a known refresh still clears cookies`(state: String) {
        val rawToken = when (state) {
            "missing" -> null
            "empty" -> ""
            "blank" -> "   "
            else -> "unknown-refresh-token"
        }
        val count = tokens.count()

        val response = mockMvc.perform(logoutRequest(rawToken, csrf()))
            .andExpect(status().isOk()).andReturn().response

        assertClearedCookies(response)
        assertEquals(count, tokens.count())
    }

    @ParameterizedTest
    @ValueSource(strings = ["expired", "revoked", "blocked"])
    fun `unusable sessions can still be logged out`(state: String) {
        val issued = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val session = tokens.findByTokenHash(sha256(issued.rawToken))!!
        val oldRevocation = Instant.parse("2026-11-01T10:00:00Z")
        when (state) {
            "expired" -> { session.expiresDate = Instant.now().minusSeconds(1); tokens.save(session) }
            "revoked" -> { session.revokedDate = oldRevocation; tokens.save(session) }
            "blocked" -> { user.isBlocked = true; users.save(user) }
        }

        val response = mockMvc.perform(logoutRequest(issued.rawToken, csrf()))
            .andExpect(status().isOk()).andReturn().response

        assertClearedCookies(response)
        val stored = tokens.findByTokenHash(sha256(issued.rawToken))!!
        assertNotNull(stored.revokedDate)
        if (state == "revoked") assertEquals(oldRevocation, stored.revokedDate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "expired", "invalid"])
    fun `logout does not require a valid access cookie`(state: String) {
        val issued = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val staleAccess = when (state) {
            "expired" -> expiredAccess()
            "invalid" -> "broken-access-token"
            else -> null
        }

        mockMvc.perform(logoutRequest(issued.rawToken, csrf(staleAccess), staleAccess))
            .andExpect(status().isOk())

        assertNotNull(tokens.findByTokenHash(sha256(issued.rawToken))!!.revokedDate)
    }

    @Test
    fun `logout preserves other sessions of the same user and another user`() {
        val current = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val anotherSession = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val foreignUser = users.save(AdminUserEntity(email = "other-logout@cardio.test", passwordHash = "test-hash"))
        otherUser = foreignUser
        val foreignSession = refreshTokenService.issueToken(foreignUser, Duration.ofDays(1))
        val csrf = csrf()

        mockMvc.perform(logoutRequest(current.rawToken, csrf)).andExpect(status().isOk())

        assertNotNull(tokens.findByTokenHash(sha256(current.rawToken))!!.revokedDate)
        assertNull(tokens.findByTokenHash(sha256(anotherSession.rawToken))!!.revokedDate)
        assertNull(tokens.findByTokenHash(sha256(foreignSession.rawToken))!!.revokedDate)
        mockMvc.perform(authRequest("/api/auth/refresh", anotherSession.rawToken, csrf))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(user.email))
        mockMvc.perform(authRequest("/api/auth/refresh", foreignSession.rawToken, csrf))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(foreignUser.email))
    }

    @Test
    fun `logout clears browser access while a copied JWT retains its original lifetime`() {
        val loginCookies = login(false)
        val copiedAccess = loginCookies.getValue("access_token").value
        val rawToken = loginCookies.getValue("refresh_token").value

        mockMvc.perform(logoutRequest(rawToken, csrf())).andExpect(status().isOk())

        mockMvc.get("/api/auth/me").andExpect { status { isUnauthorized() } }
        mockMvc.get("/api/auth/me") { cookie(Cookie("access_token", copiedAccess)) }
            .andExpect { status { isOk() } }
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "wrong", "mismatched"])
    fun `csrf failure does not revoke the session or clear authentication cookies`(state: String) {
        val issued = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val csrf = csrf()
        val request = postRequest("/api/auth/logout").cookie(Cookie("refresh_token", issued.rawToken))
        when (state) {
            "wrong" -> request.cookie(csrf.cookie).header(csrf.headerName, "invalid-csrf")
            "mismatched" -> request.cookie(csrf.cookie).header(csrf.headerName, this.csrf().token)
        }

        val response = mockMvc.perform(request)
            .andExpect(status().isForbidden())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.code").value("CSRF_TOKEN_INVALID"))
            .andExpect(jsonPath("$.instance").value("/api/auth/logout"))
            .andReturn().response

        assertNull(tokens.findByTokenHash(sha256(issued.rawToken))!!.revokedDate)
        assertTrue(response.getHeaders("Set-Cookie").none {
            it.startsWith("access_token=") || it.startsWith("refresh_token=")
        })
    }

    private fun assertClearedCookies(response: MockHttpServletResponse) {
        val cookies = responseCookies(response)
        assertEquals(setOf("access_token", "refresh_token"), cookies.keys)
        assertEquals("/", cookies.getValue("access_token").path)
        assertEquals("/api/auth", cookies.getValue("refresh_token").path)
        cookies.values.forEach {
            assertEquals("", it.value)
            assertEquals(0L, it.maxAge)
            assertTrue(it.isHttpOnly)
            assertFalse(it.secure)
        }
        response.getHeaders("Set-Cookie").forEach { assertTrue(it.contains("SameSite=Strict")) }
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

    private fun authRequest(path: String, rawToken: String?, csrf: CsrfState): MockHttpServletRequestBuilder {
        val request = postRequest(path).cookie(csrf.cookie).header(csrf.headerName, csrf.token)
        if (rawToken != null) request.cookie(Cookie("refresh_token", rawToken))
        return request
    }

    private fun logoutRequest(rawToken: String?, csrf: CsrfState, accessToken: String? = null): MockHttpServletRequestBuilder {
        val request = authRequest("/api/auth/logout", rawToken, csrf)
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
        response.getHeaders("Set-Cookie").associate {
            val cookie = HttpCookie.parse(it).single()
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
