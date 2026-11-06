package com.medical.cardio.config

import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.ProblemResponseWriter
import com.medical.cardio.exception.ApiProblem
import org.springframework.http.HttpStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Value
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfException
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher


@Configuration
class SecurityConfig(
    private val adminUserDetailsService: AdminUserDetailsService,
    private val jwtAuthFilter: JwtAuthFilter,
    private val problemResponseWriter: ProblemResponseWriter,
    @Value("\${app.auth-cookie.secure:false}") private val secureCookies: Boolean
) {
    // Контейнер для шифрования паролей
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /*
        Берем логин/пароль из запроса
        Ищем пользователя в БД через AdminUserDetailsService
        Сравниваем пароль из запроса с паролем в БД через контейнер passwordEncoder
        Если совпало - выдаем JWT-токен
     */
    @Bean
    fun authenticationManager(http: HttpSecurity): AuthenticationManager {
        val authManager = http.getSharedObject(AuthenticationManagerBuilder::class.java)
        authManager.userDetailsService(adminUserDetailsService)
            .passwordEncoder(passwordEncoder())
        return authManager.build()
    }

    /*
        CORS конфигурация
        Разрешаем запросы с фронтенда
        Разрешаем все HTTP-методы и заголовки
        Включаем поддержку заголовков авторизации и куки
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val allowedOrigins = System.getenv("ALLOWED_ORIGINS")
            ?.split(",")
            ?.map { it.trim() }
            ?: listOf("http://localhost:5173")

        val config = CorsConfiguration().apply {
            this.allowedOrigins = allowedOrigins
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf(HttpHeaders.RETRY_AFTER)
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    /*
        Применяем CORS конфигурацию
        Защищаем refresh и logout от CSRF
        ...
        Правила доступа к эндпоинтам
        Проверка access_token cookie в JwtAuthFilter
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf {
            it.csrfTokenRepository(csrfTokenRepository())
            it.requireCsrfProtectionMatcher(OrRequestMatcher(
                AntPathRequestMatcher("/api/auth/refresh", "POST"),
                AntPathRequestMatcher("/api/auth/logout", "POST")
            ))
        }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
            auth.requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
            auth.requestMatchers(
                "/api/auth/login",
                "/api/auth/logout",
                "/api/survey/**",
                "/docs",
                "/swagger-ui",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/error"
            ).permitAll()
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            auth.requestMatchers("/api/admin/super-admin/**").hasRole("SUPER_ADMIN")
            auth.requestMatchers("/api/admin/**").hasAnyRole("DOCTOR", "SUPER_ADMIN")
            auth.anyRequest().authenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint { request, response, _ ->
                val problem = ApiProblem.create(
                    status = HttpStatus.UNAUTHORIZED,
                    type = "urn:cardiocenter:problem:authentication",
                    title = "Требуется авторизация",
                    detail = "Войдите в систему для выполнения запроса.",
                    code = ApiProblem.UNAUTHORIZED
                )

                problemResponseWriter.write(request, response, problem)
            }

            it.accessDeniedHandler { request, response, exception ->
                val problem = if (exception is CsrfException) ApiProblem.create(
                    status = HttpStatus.FORBIDDEN,
                    type = "urn:cardiocenter:problem:csrf",
                    title = "Запрос отклонён",
                    detail = "Получите защитный токен через /api/auth/csrf и повторите запрос.",
                    code = ApiProblem.CSRF_TOKEN_INVALID
                ) else ApiProblem.create(
                    status = HttpStatus.FORBIDDEN,
                    type = "urn:cardiocenter:problem:forbidden",
                    title = "Доступ запрещён",
                    detail = "Недостаточно прав для выполнения запроса.",
                    code = ApiProblem.FORBIDDEN
                )

                problemResponseWriter.write(request, response, problem)
            }
        }
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun csrfTokenRepository(): CookieCsrfTokenRepository = CookieCsrfTokenRepository().apply {
        cookiePath = "/api/auth"
        setCookieCustomizer { cookie ->
            cookie.httpOnly(true).secure(secureCookies).sameSite("Strict")
        }
    }
}
