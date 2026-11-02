package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.mock.web.MockHttpServletResponse
import java.net.HttpCookie
import java.security.MessageDigest
import java.time.Duration
import java.util.HexFormat

class AuthControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "auth-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
    }

    @AfterEach
    fun tearDown() {
        adminUserRepository.delete(testUser)
    }

    @Test
    fun `login with valid credentials returns session cookies and saves one day refresh session`() {
        val sessionCount = refreshTokenRepository.count()
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("auth-test@cardio.ru") }
            jsonPath("$.role") { value("ROLE_DOCTOR") }
            jsonPath("$.accessToken") { doesNotExist() }
            jsonPath("$.refreshToken") { doesNotExist() }
            header { exists("Set-Cookie") }
        }.andReturn().response

        val cookies = authCookies(response)
        assertEquals(setOf("access_token", "refresh_token"), cookies.keys)
        assertEquals(-1L, cookies.getValue("access_token").maxAge)
        assertEquals(-1L, cookies.getValue("refresh_token").maxAge)
        assertCookieAttributes(response, cookies)
        assertRefreshSession(cookies.getValue("refresh_token").value, Duration.ofDays(1))
        assertEquals(sessionCount + 1, refreshTokenRepository.count())
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `login issues fifteen minute access token regardless of rememberMe`(rememberMe: Boolean) {
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000","rememberMe":$rememberMe}"""
        }.andExpect {
            status { isOk() }
        }.andReturn().response

        val accessCookie = requireNotNull(response.getHeader("Set-Cookie"))
        val accessToken = accessCookie.substringAfter("access_token=").substringBefore(";")
        val lifetimeMs = jwtService.extractClaim(accessToken) { claims ->
            claims.expiration.time - claims.issuedAt.time
        }

        assertEquals(900_000L, lifetimeMs)
    }

    @Test
    fun `login with wrong password returns 401`() {
        val sessionCount = refreshTokenRepository.count()
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"WrongPassword"}"""
        }.andExpect {
            status { isUnauthorized() }
            header { doesNotExist("Set-Cookie") }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @Test
    fun `login with invalid email format returns 400`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"not-an-email","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `me with valid token returns 200 with user data`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/auth/me") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("auth-test@cardio.ru") }
            jsonPath("$.role") { value("ROLE_DOCTOR") }
        }
    }

    @Test
    fun `me without token returns 401`() {
        mockMvc.get("/api/auth/me").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `logout clears both cookies with their original paths`() {
        val response = mockMvc.post("/api/auth/logout").andExpect {
            status { isOk() }
        }.andReturn().response

        val cookies = authCookies(response)
        assertEquals(setOf("access_token", "refresh_token"), cookies.keys)
        assertEquals(0L, cookies.getValue("access_token").maxAge)
        assertEquals(0L, cookies.getValue("refresh_token").maxAge)
        assertCookieAttributes(response, cookies)
    }

    @Test
    fun `login with non-existent email returns 401`() {
        val sessionCount = refreshTokenRepository.count()
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"nobody@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isUnauthorized() }
            header { doesNotExist("Set-Cookie") }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @Test
    fun `login with rememberMe persists fifteen minute access and thirty day refresh cookies`() {
        val sessionCount = refreshTokenRepository.count()
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000","rememberMe":true}"""
        }.andExpect {
            status { isOk() }
        }.andReturn().response

        val cookies = authCookies(response)
        assertEquals(setOf("access_token", "refresh_token"), cookies.keys)
        assertEquals(900L, cookies.getValue("access_token").maxAge)
        assertEquals(2_592_000L, cookies.getValue("refresh_token").maxAge)
        assertCookieAttributes(response, cookies)
        assertRefreshSession(cookies.getValue("refresh_token").value, Duration.ofDays(30))
        assertEquals(sessionCount + 1, refreshTokenRepository.count())
    }

    @Test
    fun `login with blocked doctor returns 401`() {
        val sessionCount = refreshTokenRepository.count()
        testUser.isBlocked = true
        adminUserRepository.save(testUser)

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            header { doesNotExist("Set-Cookie") }
            status { isUnauthorized() }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @Test
    fun `me with token issued before blocking returns 401`() {
        val cookie = authCookie(testUser)

        testUser.isBlocked = true
        adminUserRepository.save(testUser)

        mockMvc.get("/api/auth/me") {
            cookie(cookie)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    private fun authCookies(response: MockHttpServletResponse): Map<String, HttpCookie> =
        response.getHeaders("Set-Cookie").associate { header ->
            val cookie = HttpCookie.parse(header).single()
            cookie.name to cookie
        }

    private fun assertCookieAttributes(response: MockHttpServletResponse, cookies: Map<String, HttpCookie>) {
        assertEquals("/", cookies.getValue("access_token").path)
        assertEquals("/api/auth", cookies.getValue("refresh_token").path)
        cookies.values.forEach { cookie ->
            assertTrue(cookie.isHttpOnly)
            assertFalse(cookie.secure)
        }
        response.getHeaders("Set-Cookie").forEach { assertTrue(it.contains("SameSite=Strict")) }
    }

    private fun assertRefreshSession(rawToken: String, expectedLifetime: Duration) {
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
        val tokenHash = HexFormat.of().formatHex(hashBytes)
        val session = requireNotNull(refreshTokenRepository.findByTokenHash(tokenHash))
        assertEquals(testUser.id, session.creator.id)
        assertNotEquals(rawToken, session.tokenHash)
        assertNull(session.revokedDate)
        assertEquals(expectedLifetime, Duration.between(session.createdDate, session.expiresDate))
    }
}
