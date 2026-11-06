package com.medical.cardio.service

data class LoginAttemptCounterResult(
    val attempts: Long,                 // Кол-во зарегистрированных попыток
    val remainingTtlMs: Long,           // Оставшееся время жизни счетчика в мс
)