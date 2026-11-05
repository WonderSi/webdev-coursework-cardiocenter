package com.medical.cardio.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
@Component
class LoginRateLimitConfig(
    @Value("\${app.login-rate-limit.window-ms}")
    val windowMs: Long,

    @Value("\${app.login-rate-limit.max-attempts-per-ip}")
    val maxAttemptsPerIp: Int,

    @Value("\${app.login-rate-limit.max-attempts-per-login}")
    val maxAttemptsPerLogin: Int,
) {
    init {
        require(windowMs > 0) {
            "Login rate limit window must be positive"
        }
        require(maxAttemptsPerIp > 0) {
            "Login rate limit per IP must be positive"
        }
        require(maxAttemptsPerLogin > 0) {
            "Login rate limit per login must be positive"
        }
    }
}