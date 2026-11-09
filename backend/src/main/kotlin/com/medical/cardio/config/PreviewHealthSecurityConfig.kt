package com.medical.cardio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@Profile("preview", "local", "probe")
class PreviewHealthSecurityConfig {

    @Bean
    @Order(1)
    fun previewHealthSecurityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .securityMatcher(AntPathRequestMatcher("/actuator/health", "GET"))
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests { it.anyRequest().permitAll() }
        .build()
}