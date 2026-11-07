package com.medical.cardio.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.controller.AuthController
import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.JwtService
import com.medical.cardio.security.ProblemResponseWriter
import com.medical.cardio.security.AuthCookieService
import com.medical.cardio.service.AuthService
import com.medical.cardio.service.LoginRateLimitService
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import jakarta.servlet.http.Cookie
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.http.HttpHeaders
import org.springframework.data.redis.RedisConnectionFailureException
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.Mockito.inOrder
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post as postRequest

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class, ProblemResponseWriter::class)
class GlobalExceptionHandlerTest {

    private val loginRequest = LoginRequest(email = "test@test.com", password = "password")

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var adminUserDetailsService: AdminUserDetailsService

    @MockitoBean
    lateinit var authenticationManager: AuthenticationManager

    @MockitoBean
    lateinit var authService: AuthService

    @MockitoBean
    lateinit var authCookieService: AuthCookieService

    @MockitoBean
    lateinit var loginRateLimitService: LoginRateLimitService

    @ParameterizedTest
    @ValueSource(longs = [1, 30, 120])
    fun `login rate limit returns problem details with retry delay`(
        retryAfterSeconds: Long,
    ) {
        willThrow(LoginRateLimitExceededException(retryAfterSeconds))
            .given(loginRateLimitService)
            .checkLoginAttempt(any(), eq(loginRequest.email))

        mockMvc.post("/api/auth/login") {
            header(HttpHeaders.ORIGIN, "http://localhost:5173")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isTooManyRequests() }

            header {
                string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173")
                string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.RETRY_AFTER)
                string(HttpHeaders.RETRY_AFTER, retryAfterSeconds.toString())
                string(HttpHeaders.CACHE_CONTROL, "no-store")
                doesNotExist(HttpHeaders.SET_COOKIE)
            }

            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }

            jsonPath("$.type") { value("urn:cardiocenter:problem:login-rate-limit") }
            jsonPath("$.title") { value("Слишком много попыток входа")}
            jsonPath("$.status") { value(429) }
            jsonPath("$.detail") { value("Повторите попытку позже")}
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("LOGIN_RATE_LIMIT_EXCEEDED") }
            jsonPath("$.errors") { doesNotExist() }
        }
        verifyNoInteractions(authService, authCookieService)
    }

    @Test
    fun `unavailable rate limiter returns safe service unavailable without authentication cookies`() {
        willThrow(
            LoginRateLimitUnavailableException(
                RedisConnectionFailureException("Cannot connect to internal-redis:6379")
            )
        ).given(loginRateLimitService).checkLoginAttempt(any(), eq(loginRequest.email))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isServiceUnavailable() }
            header {
                string(HttpHeaders.CACHE_CONTROL, "no-store")
                doesNotExist(HttpHeaders.SET_COOKIE)
                doesNotExist(HttpHeaders.RETRY_AFTER)
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                string(Matchers.not(Matchers.containsString("internal-redis")))
                string(Matchers.not(Matchers.containsString("RedisConnectionFailureException")))
                string(Matchers.not(Matchers.containsString("Login rate limit check is unavailable")))
            }
            jsonPath("$.type") { value("urn:cardiocenter:problem:service-unavailable") }
            jsonPath("$.title") { value("Сервис временно недоступен") }
            jsonPath("$.status") { value(503) }
            jsonPath("$.detail") { value("Сервис временно не может выполнить запрос.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("SERVICE_UNAVAILABLE") }
            jsonPath("$.errors") { doesNotExist() }
        }
        verifyNoInteractions(authService, authCookieService)
    }

    @Test
    fun `login checks the connection address before authenticating the original email`() {
        val request = LoginRequest(email = "Doctor@Example.COM", password = "password")
        val connectionAddress = "192.0.2.10"
        given(authService.login(request)).willThrow(BadCredentialsException("Invalid credentials"))

        val response = mockMvc.perform(postRequest("/api/auth/login")
            .with { httpRequest ->
                httpRequest.remoteAddr = connectionAddress
                httpRequest
            }
            .header("X-Real-IP", "198.51.100.20")
            .header("X-Forwarded-For", "198.51.100.20")
            .header("Forwarded", "for=198.51.100.20")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andReturn().response

        assertEquals(401, response.status)
        val invocationOrder = inOrder(loginRateLimitService, authService)
        invocationOrder.verify(loginRateLimitService).checkLoginAttempt(connectionAddress, request.email)
        invocationOrder.verify(authService).login(request)
        invocationOrder.verifyNoMoreInteractions()
        verifyNoInteractions(authCookieService)
    }

    @Test
    fun `IllegalArgumentException returns generic message without internal details`() {
        val internalMessage = "JDBC URL: jdbc:postgresql://internal-host:5432/secret_db"
        given(authService.login(loginRequest))
            .willThrow(IllegalArgumentException(internalMessage))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isBadRequest() }
            content {
                string(Matchers.not(Matchers.containsString("jdbc")))
            }
            content {
                string(Matchers.not(Matchers.containsString("internal-host")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:bad-request")
            }
            jsonPath("$.title") { value("Некорректный запрос") }
            jsonPath("$.status") { value(400) }
            jsonPath("$.detail") { value("Некорректный запрос.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }

    @Test
    fun `validation error returns problem details`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"invalid-email","password":"password"}"""
        }.andExpect {
            status { isBadRequest() }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:validation")
            }
            jsonPath("$.title") { value("Ошибка валидации") }
            jsonPath("$.status") { value(400) }
            jsonPath("$.detail") { value("Исправьте отмеченные поля.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("VALIDATION_ERROR") }
            jsonPath("$.errors[0].field") { value("email") }
            jsonPath("$.errors[0].message") { isNotEmpty() }
        }
        verifyNoInteractions(loginRateLimitService, authService, authCookieService)
    }

    @Test
    fun `file too large returns problem details`() {
        given(authService.login(loginRequest))
            .willThrow(MaxUploadSizeExceededException(10L * 1024 * 1024)) // 10 MB

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isPayloadTooLarge() }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:payload-too-large")
            }
            jsonPath("$.title") { value("Превышен размер файла") }
            jsonPath("$.status") { value(413) }
            jsonPath("$.detail") { value("Файл превышает допустимый размер 10 МБ.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("PAYLOAD_TOO_LARGE") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }

    @Test
    fun `not found returns problem details without internal details`() {
        given(authService.login(loginRequest))
            .willThrow(NoSuchElementException("Resource missing on internal-host"))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isNotFound() }
            content {
                string(Matchers.not(Matchers.containsString("internal-host")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:not-found")
            }
            jsonPath("$.title") { value("Ресурс не найден") }
            jsonPath("$.status") { value(404) }
            jsonPath("$.detail") { value("Запрашиваемый ресурс не найден.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("NOT_FOUND") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }

    @Test
    fun `invalid credentials return problem details`() {
        given(authService.login(loginRequest))
            .willThrow(BadCredentialsException("Internal authentication details"))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isUnauthorized() }
            content {
                string(Matchers.not(Matchers.containsString("Internal authentication details")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:authentication")
            }
            jsonPath("$.title") { value("Ошибка входа") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Не удалось войти. Проверьте email и пароль.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("UNAUTHORIZED") }
            jsonPath("$.errors") { doesNotExist() }
            header { doesNotExist("Set-Cookie") }
        }
    }

    @Test
    fun `disabled account returns problem details`() {
        given(authService.login(loginRequest))
            .willThrow(DisabledException("Account is disabled"))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isUnauthorized() }
            content {
                string(Matchers.not(Matchers.containsString("Account is disabled")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") {
                value("urn:cardiocenter:problem:authentication")
            }
            jsonPath("$.title") { value("Ошибка входа") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Не удалось войти. Проверьте email и пароль.") }
            jsonPath("$.instance") { value("/api/auth/login") }
            jsonPath("$.code") { value("UNAUTHORIZED") }
            jsonPath("$.errors") { doesNotExist() }
            header { doesNotExist("Set-Cookie") }
        }
    }
    @Test
    fun `logout persistence failure returns safe problem without clearing cookies`() {
        willThrow(IllegalStateException("Database unavailable at internal-host"))
            .given(authService).logout("refresh-token")

        val csrfResponse = mockMvc.get("/api/auth/csrf")
            .andExpect { status { isOk() } }.andReturn().response
        val csrfBody = objectMapper.readTree(csrfResponse.contentAsString)
        val csrfCookie = requireNotNull(csrfResponse.getCookie("XSRF-TOKEN"))

        mockMvc.post("/api/auth/logout") {
            cookie(Cookie(csrfCookie.name, csrfCookie.value), Cookie("refresh_token", "refresh-token"))
            header(csrfBody.path("headerName").asText(), csrfBody.path("token").asText())
        }.andExpect {
            status { isInternalServerError() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            header { doesNotExist("Set-Cookie") }
            jsonPath("$.status") { value(500) }
            content { string(Matchers.not(Matchers.containsString("internal-host"))) }
        }
    }

    @Test
    fun `login failure returns safe problem without authentication cookies`() {
        given(authService.login(loginRequest))
            .willThrow(IllegalStateException("Database unavailable at internal-host"))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password"}"""
        }.andExpect {
            status { isInternalServerError() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            header { doesNotExist("Set-Cookie") }
            jsonPath("$.status") { value(500) }
            content { string(Matchers.not(Matchers.containsString("internal-host"))) }
        }
    }
}
