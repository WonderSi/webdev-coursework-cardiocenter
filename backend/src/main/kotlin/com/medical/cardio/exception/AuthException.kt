package com.medical.cardio.exception

import org.springframework.security.core.AuthenticationException

class TokenExpiredException(message: String = "Token has expired") : RuntimeException(message)
class InvalidTokenException(message: String = "Token is invalid") : RuntimeException(message)

class InvalidRefreshTokenException : AuthenticationException("Refresh session is invalid")
