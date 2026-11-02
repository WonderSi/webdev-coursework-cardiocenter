package com.medical.cardio.dto

/** Internal login result used to build the response body and authentication cookies. */
data class LoginResult(
    val user: LoginResponse,
    val accessToken: String,
    val refreshToken: IssuedRefreshToken
)
