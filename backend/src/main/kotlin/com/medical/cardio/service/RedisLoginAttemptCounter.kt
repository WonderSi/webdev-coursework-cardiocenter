package com.medical.cardio.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript

class RedisLoginAttemptCounter(
    private val redisTemplate: StringRedisTemplate,
    private val incrementLoginAttemptScript: RedisScript<List<*>>,
) {
    fun increment(key: String, windowMs: Long): LoginAttemptCounterResult {
        val values = checkNotNull(
            redisTemplate.execute(
                incrementLoginAttemptScript,
                listOf(key),
                windowMs.toString(),
            )
        ) {
            "Redis returned no login attempt counter result"
        }

        check(values.size == 2) {
            "Redis returned an unexpected login attempt counter result"
        }

        val attempts = checkNotNull(values[0] as? Long) {
            "Redis returned an invalid login attempt count"
        }

        val remainingTtlMs = checkNotNull(values[1] as? Long) {
            "Redis returned an invalid login attempt lifetime"
        }

        check(attempts > 0) {
            "Login attempt count must be positive"
        }

        check(remainingTtlMs >= 0) {
            "Login attempt counter must have an expiration"
        }

        return LoginAttemptCounterResult(
            attempts = attempts,
            remainingTtlMs = remainingTtlMs,
        )
    }
}