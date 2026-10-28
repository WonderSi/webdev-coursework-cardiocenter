package com.medical.cardio.security

import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.controller.AuthController
import com.medical.cardio.exception.InvalidTokenException
import com.medical.cardio.exception.TokenExpiredException
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
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.hamcrest.Matchers


@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, JwtAuthFilter::class, ProblemResponseWriter::class)
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
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") { value("urn:cardiocenter:problem:invalid-token") }
            jsonPath("$.title") { value("Недействительный токен") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Войдите в систему снова.") }
            jsonPath("$.instance") { value("/api/auth/me") }
            jsonPath("$.code") { value("INVALID_TOKEN") }
            jsonPath("$.errors") { doesNotExist() }
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
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") { value("urn:cardiocenter:problem:invalid-token") }
            jsonPath("$.title") { value("Недействительный токен") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Войдите в систему снова.") }
            jsonPath("$.instance") { value("/api/auth/me") }
            jsonPath("$.code") { value("INVALID_TOKEN") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }

    @Test
    fun `request without cookie to protected endpoint returns 401`() {
        mockMvc.get("/api/auth/me")
            .andExpect {
                status { isUnauthorized() }
                content {
                    contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                }
                jsonPath("$.type") { value("urn:cardiocenter:problem:authentication") }
                jsonPath("$.title") { value("Требуется авторизация") }
                jsonPath("$.status") { value(401) }
                jsonPath("$.detail") { value("Войдите в систему для выполнения запроса.") }
                jsonPath("$.instance") { value("/api/auth/me") }
                jsonPath("$.code") { value("UNAUTHORIZED") }
                jsonPath("$.errors") { doesNotExist() }
            }
    }

    @Test
    fun `expired JWT cookie returns problem details`() {
        given(jwtService.extractUsername("expired.token"))
            .willThrow(TokenExpiredException("Internal expiration details"))

        mockMvc.get("/api/auth/me") {
            cookie(Cookie("access_token", "expired.token"))
        }.andExpect {
            status { isUnauthorized() }
            content {
                string(Matchers.not(Matchers.containsString("Internal expiration details")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") { value("urn:cardiocenter:problem:token-expired") }
            jsonPath("$.title") { value("Сессия истекла") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Войдите в систему снова.") }
            jsonPath("$.instance") { value("/api/auth/me") }
            jsonPath("$.code") { value("TOKEN_EXPIRED") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }

    @Test
    fun `JWT for missing user returns invalid token problem details`() {
        given(jwtService.extractUsername("missing-user.token"))
            .willReturn("missing@example.com")

        given(adminUserDetailsService.loadUserByUsername("missing@example.com"))
            .willThrow(UsernameNotFoundException("Internal user details"))

        mockMvc.get("/api/auth/me") {
            cookie(Cookie("access_token", "missing-user.token"))
        }.andExpect {
            status { isUnauthorized() }
            content {
                string(Matchers.not(Matchers.containsString("Internal user details")))
            }
            content {
                contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
            }
            jsonPath("$.type") { value("urn:cardiocenter:problem:invalid-token") }
            jsonPath("$.title") { value("Недействительный токен") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("Войдите в систему снова.") }
            jsonPath("$.instance") { value("/api/auth/me") }
            jsonPath("$.code") { value("INVALID_TOKEN") }
            jsonPath("$.errors") { doesNotExist() }
        }
    }
}
