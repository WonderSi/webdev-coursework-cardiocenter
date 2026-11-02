package com.medical.cardio.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration

class RefreshTokenConfigTest {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `lifetimeFor uses configured durations`(rememberMe: Boolean) {
        val regularLifetime = Duration.ofHours(3)
        val rememberedLifetime = Duration.ofDays(7)
        val config = RefreshTokenConfig(regularLifetime.toMillis(), rememberedLifetime.toMillis())

        assertEquals(
            if (rememberMe) rememberedLifetime else regularLifetime,
            config.lifetimeFor(rememberMe)
        )
    }

    @ParameterizedTest
    @CsvSource("0,1", "-1,1", "1,0", "1,-1", "2,1")
    fun `invalid lifetimes fail during configuration creation`(regularMs: Long, rememberedMs: Long) {
        assertThrows<IllegalArgumentException> {
            RefreshTokenConfig(regularMs, rememberedMs)
        }
    }
}
