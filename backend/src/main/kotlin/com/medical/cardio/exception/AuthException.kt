package com.medical.cardio.exception

class TokenExpiredException(message: String = "Token has expired") : RuntimeException(message)
class InvalidTokenException(message: String = "Token is invalid") : RuntimeException(message)
