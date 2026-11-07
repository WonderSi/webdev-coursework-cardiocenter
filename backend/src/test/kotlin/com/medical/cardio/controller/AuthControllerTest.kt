package com.medical.cardio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import com.medical.cardio.support.ControllerTestBase
import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.service.LoginAttemptCounterResult
import com.medical.cardio.service.RedisLoginAttemptCounter
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
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.BDDMockito.given
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.security.crypto.password.PasswordEncoder
import jakarta.servlet.http.Cookie
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

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var loginAttemptCounter: RedisLoginAttemptCounter

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

    @ParameterizedTest
    @ValueSource(strings = ["auth-test@cardio.ru", "nobody@cardio.ru"])
    fun `repeated invalid credentials reach the same email limit for existing and unknown accounts`(email: String) {
        given(loginAttemptCounter.increment(argThat { startsWith("auth:login:email:") }, any()))
            .willReturn(
                LoginAttemptCounterResult(1L, 60000L),
                LoginAttemptCounterResult(2L, 60000L),
                LoginAttemptCounterResult(3L, 60000L),
                LoginAttemptCounterResult(4L, 60000L),
                LoginAttemptCounterResult(5L, 60000L),
                LoginAttemptCounterResult(6L, 60000L),
            )
        val sessionCount = refreshTokenRepository.count()
        val content = objectMapper.writeValueAsString(LoginRequest(email = email, password = "WrongPassword"))

        repeat(5) {
            mockMvc.post("/api/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                this.content = content
            }.andExpect {
                status { isUnauthorized() }
                header { doesNotExist("Set-Cookie") }
                jsonPath("$.code") { value("UNAUTHORIZED") }
            }
        }

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            this.content = content
        }.andExpect {
            status { isTooManyRequests() }
            header {
                string("Retry-After", "60")
                string("Cache-Control", "no-store")
                doesNotExist("Set-Cookie")
            }
            jsonPath("$.code") { value("LOGIN_RATE_LIMIT_EXCEEDED") }
            jsonPath("$.detail") { value("Повторите попытку позже") }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @ParameterizedTest
    @CsvSource("ip,21", "email,6")
    fun `exceeded limits reject valid credentials without creating a session`(counterType: String, attempts: Long) {
        given(loginAttemptCounter.increment(argThat { startsWith("auth:login:$counterType:") }, any()))
            .willReturn(LoginAttemptCounterResult(attempts, 1001L))
        val sessionCount = refreshTokenRepository.count()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isTooManyRequests() }
            header {
                string("Retry-After", "2")
                string("Cache-Control", "no-store")
                doesNotExist("Set-Cookie")
            }
            jsonPath("$.code") { value("LOGIN_RATE_LIMIT_EXCEEDED") }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    @ParameterizedTest
    @CsvSource("ip,connection", "ip,timeout", "email,connection", "email,timeout")
    fun `unavailable counters reject valid credentials without creating a session`(
        counterType: String,
        failureType: String,
    ) {
        val cause = when (failureType) {
            "connection" -> RedisConnectionFailureException("Cannot connect to internal-redis:6379")
            "timeout" -> QueryTimeoutException("Redis request timed out")
            else -> error("Unknown failure type: $failureType")
        }
        given(loginAttemptCounter.increment(argThat { startsWith("auth:login:$counterType:") }, any()))
            .willThrow(cause)
        val sessionCount = refreshTokenRepository.count()

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isServiceUnavailable() }
            header {
                string("Cache-Control", "no-store")
                doesNotExist("Retry-After")
                doesNotExist("Set-Cookie")
            }
            jsonPath("$.code") { value("SERVICE_UNAVAILABLE") }
            jsonPath("$.detail") { value("Сервис временно не может выполнить запрос.") }
        }
        assertEquals(sessionCount, refreshTokenRepository.count())
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
        val csrfResponse = mockMvc.get("/api/auth/csrf")
            .andExpect { status { isOk() } }.andReturn().response
        val csrfBody = objectMapper.readTree(csrfResponse.contentAsString)
        val csrfCookie = requireNotNull(csrfResponse.getCookie("XSRF-TOKEN"))
        val response = mockMvc.post("/api/auth/logout") {
            cookie(Cookie(csrfCookie.name, csrfCookie.value))
            header(csrfBody.path("headerName").asText(), csrfBody.path("token").asText())
        }.andExpect {
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
        assertRefreshSession(cookies.getValue("refresh_token").value, Duration.ofDays(30), rememberMe = true)
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

    private fun assertRefreshSession(rawToken: String, expectedLifetime: Duration, rememberMe: Boolean = false) {
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
        val tokenHash = HexFormat.of().formatHex(hashBytes)
        val session = requireNotNull(refreshTokenRepository.findByTokenHash(tokenHash))
        assertEquals(testUser.id, session.creator.id)
        assertNotEquals(rawToken, session.tokenHash)
        assertNull(session.revokedDate)
        assertEquals(rememberMe, session.rememberMe)
        assertEquals(expectedLifetime, Duration.between(session.createdDate, session.expiresDate))
    }
}
