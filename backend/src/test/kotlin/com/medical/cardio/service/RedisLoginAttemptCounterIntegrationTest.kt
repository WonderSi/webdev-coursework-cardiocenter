package com.medical.cardio.service

import com.medical.cardio.config.LoginRateLimitRedisConfig
import com.medical.cardio.support.RedisTestContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.awaitility.Awaitility.await
import java.time.Duration
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RedisLoginAttemptCounterIntegrationTest {

    @Test
    fun `increment executes Lua against Redis`() {
        withRedisCounter { counter, _ ->
            val key = "auth:login:integration:${UUID.randomUUID()}"
            val windowMs = 60000L

            val firstResult = counter.increment(key, windowMs)
            val secondResult = counter.increment(key, windowMs)

            assertEquals(1L, firstResult.attempts)
            assertEquals(2L, secondResult.attempts)
            assertTrue(firstResult.remainingTtlMs in 0L..windowMs)
            assertTrue(secondResult.remainingTtlMs in 0L..windowMs)
        }
    }

    private fun withRedisCounter(
        test: (RedisLoginAttemptCounter, StringRedisTemplate) -> Unit,
    ) {
        RedisTestContainer().use { container ->
            val redisConfig = RedisStandaloneConfiguration(
                "127.0.0.1",
                container.port,
            )
            val clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build()

            val connectionFactory = LettuceConnectionFactory(redisConfig, clientConfig)

            try {
                connectionFactory.afterPropertiesSet()
                connectionFactory.start()

                val redisTemplate = StringRedisTemplate(connectionFactory)
                val script = LoginRateLimitRedisConfig().incrementLoginAttemptScript()
                val counter = RedisLoginAttemptCounter(redisTemplate, script)

                test(counter, redisTemplate)
            } finally {
                connectionFactory.destroy()
            }
        }
    }

    @Test
    fun `increment preserves existing expiration`() {
        withRedisCounter { counter, redisTemplate ->
            val key = "auth:login:integration:${UUID.randomUUID()}"
            val windowMs = 120000L
            val shortenedLifetime = Duration.ofSeconds(30)

            counter.increment(key, windowMs)

            assertEquals(
                true,
                redisTemplate.expire(key, shortenedLifetime),
            )

            val result = counter.increment(key, windowMs)

            assertEquals(2L, result.attempts)
            assertTrue(
                result.remainingTtlMs in 0L..shortenedLifetime.toMillis()
            )
        }
    }

    @Test
    fun `increment starts a new counter after expiration`() {
        withRedisCounter { counter, redisTemplate ->
            val key = "auth:login:integration:${UUID.randomUUID()}"
            val windowMs = 60000L

            counter.increment(key, windowMs)

            assertEquals(
                true,
                redisTemplate.expire(key, Duration.ofMillis(100)),
            )

            await()
                .atMost(Duration.ofSeconds(5))
                .until { redisTemplate.hasKey(key) == false }

            val result = counter.increment(key, windowMs)

            assertEquals(1L, result.attempts)
            assertTrue(result.remainingTtlMs in 0L..windowMs)
        }
    }

    @Test
    fun `increment keeps different keys independent`() {
        withRedisCounter { counter, redisTemplate ->
            val firstKey = "auth:login:integration:${UUID.randomUUID()}"
            val secondKey = "auth:login:integration:${UUID.randomUUID()}"
            val windowMs = 60000L

            counter.increment(firstKey, windowMs)
            val firstResult = counter.increment(firstKey, windowMs)
            val secondResult = counter.increment(secondKey, windowMs)

            assertEquals(2L, firstResult.attempts)
            assertEquals(1L, secondResult.attempts)

            assertEquals("2", redisTemplate.opsForValue().get(firstKey))
            assertEquals("1", redisTemplate.opsForValue().get(secondKey))
        }
    }

    @Test
    fun `increment counts concurrent requests without losing attempts`() {
        withRedisCounter { counter, redisTemplate ->
            val key = "auth:login:integration:${UUID.randomUUID()}"
            val windowMs = 60000L
            val requestCount = 8
            val executor = Executors.newFixedThreadPool(requestCount)
            val barrier = CyclicBarrier(requestCount)

            try {
                val futures = (1..requestCount).map {
                    executor.submit(Callable {
                        barrier.await(5, TimeUnit.SECONDS)
                        counter.increment(key, windowMs)
                    })
                }

                val results = futures.map {
                    it.get(10, TimeUnit.SECONDS)
                }

                assertEquals(
                    (1L..requestCount.toLong()).toList(),
                    results.map { it.attempts }.sorted(),
                )
                assertTrue(
                    results.all { it.remainingTtlMs in 0L..windowMs }
                )
                assertEquals(
                    requestCount.toString(),
                    redisTemplate.opsForValue().get(key),
                )
            } finally {
                executor.shutdownNow()
                assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS))
            }
        }
    }
}