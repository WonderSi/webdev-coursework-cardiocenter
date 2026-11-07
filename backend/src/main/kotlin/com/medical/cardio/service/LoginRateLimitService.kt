package com.medical.cardio.service

import com.medical.cardio.config.LoginRateLimitConfig
import com.medical.cardio.exception.LoginRateLimitExceededException
import com.medical.cardio.exception.LoginRateLimitUnavailableException
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.redis.RedisConnectionFailureException
import java.security.MessageDigest
import java.util.HexFormat
import java.util.Locale

class LoginRateLimitService(
    private val counter: RedisLoginAttemptCounter,
    private val config: LoginRateLimitConfig,
) {
    fun checkLoginAttempt(ipAddress: String, email: String) {
        val ipKey = "auth:login:ip:${hashKeyPart(ipAddress)}"
        checkLimit(ipKey, config.maxAttemptsPerIp)

        val normalizedEmail = email.trim().lowercase(Locale.ROOT)
        val emailKey = "auth:login:email:${hashKeyPart(normalizedEmail)}"
        checkLimit(emailKey, config.maxAttemptsPerLogin)
    }

    private fun checkLimit(key: String, maxAttempts: Int) {
        val result = try {
            counter.increment(key, config.windowMs)
        } catch (exception: RedisConnectionFailureException) {
            throw LoginRateLimitUnavailableException(exception)
        } catch (exception: QueryTimeoutException) {
            throw LoginRateLimitUnavailableException(exception)
        }

        if (result.attempts <= maxAttempts.toLong()) {
            return
        }

        val retryAfterSeconds = Math.ceilDiv(
            result.remainingTtlMs,
            1000L,
        ).coerceAtLeast(1L)

        throw LoginRateLimitExceededException(retryAfterSeconds)
    }

    private fun hashKeyPart(value: String): String {
        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))

        return HexFormat.of().formatHex(hashBytes)
    }
}