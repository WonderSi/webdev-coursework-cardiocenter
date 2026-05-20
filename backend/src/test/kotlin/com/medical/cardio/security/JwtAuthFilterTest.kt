package com.medical.cardio.security

import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.controller.AuthController
import com.medical.cardio.exception.InvalidTokenException
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class)
class JwtAuthFilterTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var adminUserDetailsService: AdminUserDetailsService

    @MockitoBean
    lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `invalid JWT cookie returns 401 with error body`() {
        given(jwtService.extractUsername("bad.token.here"))
            .willThrow(InvalidTokenException("bad token"))

        mockMvc.get("/api/auth/me") {
            cookie(Cookie("access_token", "bad.token.here"))
        }.andExpect {
            status { isUnauthorized() }
            content { json("""{"error":"invalid_token"}""") }
        }
    }

    @Test
    fun `malformed JWT cookie returns 401 with error body`() {
        given(jwtService.extractUsername("notajwtatall"))
            .willThrow(InvalidTokenException("malformed token"))

        mockMvc.get("/api/auth/me") {
            cookie(Cookie("access_token", "notajwtatall"))
        }.andExpect {
            status { isUnauthorized() }
            content { json("""{"error":"invalid_token"}""") }
        }
    }

    @Test
    fun `request without cookie to protected endpoint returns 403`() {
        mockMvc.get("/api/auth/me")
            .andExpect {
                status { isForbidden() }
            }
    }
}
