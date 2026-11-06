package com.medical.cardio.exception

class LoginRateLimitExceededException(
    val retryAfterSeconds: Long,
) : RuntimeException("Login rate limit exceeded")