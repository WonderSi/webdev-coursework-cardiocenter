package com.medical.cardio.security

import com.medical.cardio.config.RefreshTokenConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration

class AuthCookieServiceTest {
    private val refreshConfig = RefreshTokenConfig(
        Duration.ofDays(1).toMillis(),
        Duration.ofDays(30).toMillis()
    )

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `session cookies use required flags paths and configured secure setting`(secure: Boolean) {
        val service = AuthCookieService(secure, 900_000, refreshConfig)
        val accessCookie = service.accessToken("access-token", false)
        val refreshCookie = service.refreshToken("refresh-token", false)

        assertEquals("/", accessCookie.path)
        assertEquals("/api/auth", refreshCookie.path)
        listOf(accessCookie, refreshCookie).forEach { cookie ->
            assertTrue(cookie.isHttpOnly)
            assertEquals(secure, cookie.isSecure)
            assertEquals("Strict", cookie.sameSite)
            assertTrue(cookie.maxAge.isNegative)
        }
    }

    @Test
    fun `remembered cookies use configured access and refresh lifetimes`() {
        val config = RefreshTokenConfig(Duration.ofHours(3).toMillis(), Duration.ofDays(7).toMillis())
        val service = AuthCookieService(true, Duration.ofMinutes(10).toMillis(), config)

        assertEquals(Duration.ofMinutes(10), service.accessToken("access-token", true).maxAge)
        assertEquals(Duration.ofDays(7), service.refreshToken("refresh-token", true).maxAge)
    }

    @Test
    fun `clearing cookies preserves paths and security flags`() {
        val service = AuthCookieService(true, 900_000, refreshConfig)
        val accessCookie = service.clearAccessToken()
        val refreshCookie = service.clearRefreshToken()

        assertEquals("access_token", accessCookie.name)
        assertEquals("refresh_token", refreshCookie.name)
        assertEquals("/", accessCookie.path)
        assertEquals("/api/auth", refreshCookie.path)
        listOf(accessCookie, refreshCookie).forEach { cookie ->
            assertEquals("", cookie.value)
            assertEquals(Duration.ZERO, cookie.maxAge)
            assertTrue(cookie.isSecure)
            assertTrue(cookie.isHttpOnly)
            assertEquals("Strict", cookie.sameSite)
        }
    }
}
