package com.medical.cardio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.data.redis.core.script.RedisScript

@Configuration
class LoginRateLimitRedisConfig {

    /**
     * Назначение:
     * Регистрирует Lua-скрипт счётчика попыток входа как bean Spring.
     *
     * Поведение скрипта при запуске:
     * 1. Увеличивает счётчик на единицу.
     * 2. При первой попытке устанавливает срок жизни.
     * 3. Возвращает количество попыток и оставшееся время.
     *
     * Формат результата:
     * [0] — количество попыток;
     * [1] — оставшееся время в миллисекундах.
     *
     * Особенности:
     * Операции выполняются атомарно.
     * Повторные попытки не продлевают срок жизни счётчика.
     * Этот метод регистрирует скрипт; выполнение в Redis вызывается отдельно.
     */
    @Bean
    fun incrementLoginAttemptScript(): RedisScript<List<*>> {
        val script = DefaultRedisScript<List<*>>()
        script.setLocation(
            ClassPathResource("redis/increment-login-attempt.lua")
        )
        script.resultType = List::class.java
        return script
    }
}