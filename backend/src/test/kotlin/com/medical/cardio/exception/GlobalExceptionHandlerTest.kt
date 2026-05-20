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

        // аутентификация проходит успешно
        given(authenticationManager.authenticate(any()))
            .willReturn(UsernamePasswordAuthenticationToken("test@test.com", "password", emptyList()))
        given(adminUserDetailsService.loadUserByUsername("test@test.com")).willReturn(userDetails)
        // но генерация токена бросает IllegalArgumentException с внутренними деталями
        given(jwtService.generateToken(userDetails))
            .willThrow(IllegalArgumentException(internalMessage))

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"test@test.com","password":"password","rememberMe":false}"""
        }.andExpect {
            status { isBadRequest() }
            // клиент видит только generic-сообщение
            content { json("""{"error":"Некорректный запрос"}""") }
            // внутренние детали не утекают в ответ
            content { string(Matchers.not(Matchers.containsString("jdbc"))) }
            content { string(Matchers.not(Matchers.containsString("internal-host"))) }
        }
    }
}
