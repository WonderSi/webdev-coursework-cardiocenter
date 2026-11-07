package com.medical.cardio.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.assertDoesNotThrow

class LoginRateLimitConfigTest {

    @ParameterizedTest
    @CsvSource(
        "0,20,5",
        "-1,20,5",
        "60000,0,5",
        "60000,-1,5",
        "60000,20,0",
        "60000,20,-1"
    )
    fun `non-positive settings are rejected`(
        windowMs: Long,
        maxAttemptsPerIp: Int,
        maxAttemptsPerLogin: Int,
    ) {
        assertThrows<IllegalArgumentException> {
            LoginRateLimitConfig(
                windowMs = windowMs,
                maxAttemptsPerIp = maxAttemptsPerIp,
                maxAttemptsPerLogin = maxAttemptsPerLogin,
            )
        }
    }

    @Test
    fun `minimum positive settings are accepted`() {
        assertDoesNotThrow {
            LoginRateLimitConfig(
                windowMs = 1L,
                maxAttemptsPerIp = 1,
                maxAttemptsPerLogin = 1,
            )
        }
    }
}