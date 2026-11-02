package com.medical.cardio.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenConfig(
    @Value("\${app.refresh-token.expiration-ms}")
    val expirationMs: Long,

    @Value("\${app.refresh-token.remember-me-expiration-ms}")
    val rememberMeExpirationMs: Long,
) {
    init {
        require(expirationMs > 0) { "Refresh token lifetime must be positive" }
        require(rememberMeExpirationMs >= expirationMs) {
            "Remembered refresh token lifetime must not be shorter than the regular lifetime"
        }
    }

    fun lifetimeFor(rememberMe: Boolean): Duration {
        val lifetimeMs = if (rememberMe) rememberMeExpirationMs else expirationMs
        return Duration.ofMillis(lifetimeMs)
    }
}
