package com.medical.cardio.exception

class LoginRateLimitUnavailableException(cause: Throwable) :
        RuntimeException("Login rate limit check is unavailable", cause)