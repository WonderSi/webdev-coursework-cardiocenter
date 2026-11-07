package com.medical.cardio.service

import com.medical.cardio.config.LoginRateLimitConfig
import com.medical.cardio.exception.LoginRateLimitExceededException
import com.medical.cardio.exception.LoginRateLimitUnavailableException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.kotlin.argThat
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException

class LoginRateLimitServiceTest {
    private val counter = mock<RedisLoginAttemptCounter>()
    private val config = LoginRateLimitConfig(60000L, 20, 5)
    private val service = LoginRateLimitService(counter, config)

    @ParameterizedTest
    @CsvSource("1,1", "20,5")
    fun `attempts within both limits are allowed`(ipAttempts: Long, emailAttempts: Long) {
        stubCounters(ipAttempts, emailAttempts, 1000L)

        assertDoesNotThrow {
            service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }
    }

    @ParameterizedTest
    @CsvSource(
        "21,1,0,1",
        "21,1,1,1",
        "21,1,999,1",
        "21,1,1000,1",
        "21,1,1001,2",
        "1,6,1001,2",
    )
    fun `exceeding either limit reports a rounded retry delay`(
        ipAttempts: Long,
        emailAttempts: Long,
        remainingTtlMs: Long,
        expectedRetryAfterSeconds: Long,
    ) {
        stubCounters(ipAttempts, emailAttempts, remainingTtlMs)

        val exception = assertThrows<LoginRateLimitExceededException> {
            service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }

        assertEquals(expectedRetryAfterSeconds, exception.retryAfterSeconds)
    }

    @ParameterizedTest
    @CsvSource("3,1", "1,2")
    fun `configured window and limits are used`(ipAttempts: Long, emailAttempts: Long) {
        val customConfig = LoginRateLimitConfig(2500L, 2, 1)
        val customService = LoginRateLimitService(counter, customConfig)
        stubCounters(ipAttempts, emailAttempts, 1000L, customConfig.windowMs)

        val exception = assertThrows<LoginRateLimitExceededException> {
            customService.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }

        assertEquals(1L, exception.retryAfterSeconds)
    }

    @ParameterizedTest
    @ValueSource(strings = [" doctor@example.com ", "DOCTOR@EXAMPLE.COM", " Doctor@Example.Com "])
    fun `email keys ignore surrounding whitespace and letter case`(email: String) {
        stubCounters(1L, 1L, 1000L)

        service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        service.checkLoginAttempt("192.0.2.10", email)

        val keys = capturedKeys(4)
        assertEquals(keys[1], keys[3])
    }

    @Test
    fun `IP and email identities have independent keys`() {
        stubCounters(1L, 1L, 1000L)

        service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        service.checkLoginAttempt("192.0.2.11", "doctor@example.com")
        service.checkLoginAttempt("192.0.2.10", "other@example.com")

        val keys = capturedKeys(6)
        assertNotEquals(keys[0], keys[2], "Different IPs must have different counters")
        assertEquals(keys[0], keys[4], "Changing email must not change the IP counter")
        assertEquals(keys[1], keys[3], "Changing IP must not change the email counter")
        assertNotEquals(keys[1], keys[5], "Different emails must have different counters")
    }

    @Test
    fun `counter keys have separate namespaces and fixed length hashed identifiers`() {
        stubCounters(1L, 1L, 1000L)

        service.checkLoginAttempt("192.0.2.10", "doctor@example.com")

        val keys = capturedKeys(2)
        assertTrue(keys[0].matches(Regex("auth:login:ip:[0-9a-f]{64}")))
        assertTrue(keys[1].matches(Regex("auth:login:email:[0-9a-f]{64}")))
    }

    @Test
    fun `exceeded IP limit stops before the email counter`() {
        stubCounters(21L, 6L, 1000L)

        assertThrows<LoginRateLimitExceededException> {
            service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }

        val keys = capturedKeys(1)
        assertTrue(keys.single().startsWith("auth:login:ip:"))
    }

    @ParameterizedTest
    @CsvSource("ip,connection", "ip,timeout", "email,connection", "email,timeout")
    fun `Redis connection failures and timeouts prevent the attempt and preserve the cause`(
        counterType: String,
        failureType: String,
    ) {
        stubCounters(1L, 1L, 1000L)
        val cause = when (failureType) {
            "connection" -> RedisConnectionFailureException("Cannot connect to internal-redis:6379")
            "timeout" -> QueryTimeoutException("Redis request timed out")
            else -> error("Unknown failure type: $failureType")
        }
        given(counter.increment(
            argThat { startsWith("auth:login:$counterType:") },
            eq(config.windowMs),
        )).willThrow(cause)

        val exception = assertThrows<LoginRateLimitUnavailableException> {
            service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }

        assertSame(cause, exception.cause)
        val keys = capturedKeys(if (counterType == "ip") 1 else 2)
        assertTrue(keys.first().startsWith("auth:login:ip:"))
    }

    @Test
    fun `unexpected counter errors are propagated without an unavailable wrapper`() {
        val cause = IllegalStateException("Invalid counter reply")
        given(counter.increment(
            argThat { startsWith("auth:login:ip:") },
            eq(config.windowMs),
        )).willThrow(cause)

        val exception = assertThrows<IllegalStateException> {
            service.checkLoginAttempt("192.0.2.10", "doctor@example.com")
        }

        assertSame(cause, exception)
    }

    private fun stubCounters(
        ipAttempts: Long,
        emailAttempts: Long,
        remainingTtlMs: Long,
        windowMs: Long = config.windowMs,
    ) {
        given(counter.increment(
            argThat { startsWith("auth:login:ip:") },
            eq(windowMs),
        )).willReturn(LoginAttemptCounterResult(ipAttempts, remainingTtlMs))

        given(counter.increment(
            argThat { startsWith("auth:login:email:") },
            eq(windowMs),
        )).willReturn(LoginAttemptCounterResult(emailAttempts, remainingTtlMs))
    }

    private fun capturedKeys(expectedCalls: Int): List<String> {
        val keys = argumentCaptor<String>()
        verify(counter, times(expectedCalls)).increment(keys.capture(), eq(config.windowMs))
        verifyNoMoreInteractions(counter)
        return keys.allValues
    }
}
