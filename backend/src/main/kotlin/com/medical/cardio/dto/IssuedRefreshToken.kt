package com.medical.cardio.dto

import java.time.Instant

data class IssuedRefreshToken(
    val rawToken: String,
    val expiresDate: Instant
)