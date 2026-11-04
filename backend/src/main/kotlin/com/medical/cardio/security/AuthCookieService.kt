package com.medical.cardio.security

import com.medical.cardio.config.RefreshTokenConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AuthCookieService(
    @Value("\${app.auth-cookie.secure:false}") private val secure: Boolean,
    @Value("\${app.jwt.expiration-ms}") private val accessExpirationMs: Long,
    private val refreshTokenConfig: RefreshTokenConfig
) {
    init {
        require(accessExpirationMs > 0) { "Access token lifetime must be positive" }
    }

    fun accessToken(token: String, rememberMe: Boolean): ResponseCookie {
        val builder = cookie("access_token", token, "/")
        if (rememberMe) builder.maxAge(Duration.ofMillis(accessExpirationMs))
        return builder.build()
    }

    fun refreshToken(token: String, rememberMe: Boolean): ResponseCookie {
        val builder = cookie("refresh_token", token, "/api/auth")
        if (rememberMe) builder.maxAge(refreshTokenConfig.lifetimeFor(true))
        return builder.build()
    }

    fun clearAccessToken(): ResponseCookie =
        cookie("access_token", "", "/").maxAge(Duration.ZERO).build()

    fun clearRefreshToken(): ResponseCookie =
        cookie("refresh_token", "", "/api/auth").maxAge(Duration.ZERO).build()

    private fun cookie(name: String, value: String, path: String): ResponseCookie.ResponseCookieBuilder =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .sameSite("Strict")
            .path(path)
}
