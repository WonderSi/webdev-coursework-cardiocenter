package com.medical.cardio.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.BDDMockito.given
import org.mockito.kotlin.mock
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript



class RedisLoginAttemptCounterTest {

    private val redisTemplate = mock<StringRedisTemplate>()
    private val script = mock<RedisScript<List<*>>>()
    private val counter = RedisLoginAttemptCounter(redisTemplate, script)

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 3])
    fun `increment rejects Redis responses with unexpected size`(
        responseSize: Int,
    ) {
        val key = "auth:login:ip:192.0.2.10"
        val windowMs = 60000L
        val response = List(responseSize) { 1L }

        given(redisTemplate.execute(script, listOf(key), windowMs.toString()))
            .willReturn(response)

        assertThrows<IllegalStateException> {
            counter.increment(key, windowMs)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1])
    fun `increment rejects response elements with unexpected types`(
        invalidIndex: Int,
    ) {
        val key = "auth:login:ip:192.0.2.10"
        val windowMs = 60000L
        val response = mutableListOf<Any>(3L, 45000L)
        response[invalidIndex] = "invalid"

        given(redisTemplate.execute(script, listOf(key), windowMs.toString()))
            .willReturn(response)

        assertThrows<IllegalStateException> {
            counter.increment(key, windowMs)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "0,45000",
        "-1,45000",
        "1,-1",
        "1,-2",
    )
    fun `increment rejects invalid counter values`(
        attempts: Long,
        remainingTtlMs: Long,
    ) {
        val key = "auth:login:ip:192.0.2.10"
        val windowMs = 60000L

        given(redisTemplate.execute(script, listOf(key), windowMs.toString()))
            .willReturn(listOf(attempts, remainingTtlMs))

        assertThrows<IllegalStateException> {
            counter.increment(key, windowMs)
        }
    }

    @ParameterizedTest
    @CsvSource(
        "3,45000",
        "1,0",
    )
    fun `increment converts Redis response to named result`(
        attempts: Long,
        remainingTtlMs: Long,
    ) {
        val key = "auth:login:ip:192.0.2.10"
        val windowMs = 60000L

        given(redisTemplate.execute(script, listOf(key), windowMs.toString()))
            .willReturn(listOf(attempts, remainingTtlMs))

        val result = counter.increment(key, windowMs)

        assertEquals(
            LoginAttemptCounterResult(
                attempts = attempts,
                remainingTtlMs = remainingTtlMs,
            ),
            result,
        )
    }

    @Test
    fun `increment rejects missing Redis response`() {
        val key = "auth:login:ip:192.0.2.10"
        val windowMs = 60000L

        given(redisTemplate.execute(script, listOf(key), windowMs.toString()))
            .willReturn(null)

        assertThrows<IllegalStateException> {
            counter.increment(key, windowMs)
        }
    }
}