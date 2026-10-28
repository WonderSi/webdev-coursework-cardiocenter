package com.medical.cardio.exception

import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.controller.AuthController
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.JwtService
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.web.multipart.MaxUploadSizeExceededException

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class GlobalExceptionHandlerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var adminUserDetailsService: AdminUserDetailsService

    @MockitoBean
    lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `IllegalArgumentException returns generic message without internal details`() {
        val internalMessage = "JDBC URL: jdbc:postgresql://internal-host:5432/secret_db"
        val userDetails = User("test@test.com", "", listOf(SimpleGrantedAuthority("ROLE_DOCTOR")))

        given(authenticationManager.authenticate(any()))
            .willReturn(UsernamePasswordAuthenticationToken("test@test.com", "password", emptyList()))
        given(adminUserDetailsService.loadUserByUsername("test@test.com")).willReturn(userDetails)
        given(jwtService.generateToken(userDetails))
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
    }

    @Test
    fun `file too large returns problem details`() {
        val userDetails = User("test@test.com", "", listOf(SimpleGrantedAuthority("ROLE_DOCTOR")))

        given(authenticationManager.authenticate(any()))
            .willReturn(UsernamePasswordAuthenticationToken("test@test.com", "password", emptyList()))
        given(adminUserDetailsService.loadUserByUsername("test@test.com")).willReturn(userDetails)
        given(jwtService.generateToken(userDetails))
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
        val userDetails = User("test@test.com", "", listOf(SimpleGrantedAuthority("ROLE_DOCTOR")))

        given(authenticationManager.authenticate(any()))
            .willReturn(UsernamePasswordAuthenticationToken("test@test.com", "password", emptyList()))
        given(adminUserDetailsService.loadUserByUsername("test@test.com")).willReturn(userDetails)
        given(jwtService.generateToken(userDetails))
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
}
