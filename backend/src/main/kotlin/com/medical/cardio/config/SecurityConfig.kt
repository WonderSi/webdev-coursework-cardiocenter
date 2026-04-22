package com.medical.cardio.config

import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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

@Configuration
class SecurityConfig(
    private val adminUserDetailsService: AdminUserDetailsService,
    private val jwtAuthFilter: JwtAuthFilter
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
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }

    /*
        Применяем CORS конфигурацию
        Отключаем CSRF (используем JWT)
        ...
        Праила доступа к эндпоинтам
        Проверка токена из Authorization
     */
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { auth ->
            auth.requestMatchers(
                "/api/auth/**",
                "/api/survey/**",
                "/api/glossaries/**",
                "/actuator/**",
                "/swagger-ui",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs",
                "/v3/api-docs/**"
            ).permitAll()
            auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            auth.requestMatchers("/api/admin/extended/**").hasRole("DOCTOR_EXTENDED")
            auth.requestMatchers("/api/admin/**").hasAnyRole("DOCTOR", "DOCTOR_EXTENDED")
            auth.anyRequest().authenticated()
        }
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()
}
