package com.medical.cardio.dto

data class RefreshResult(
    val user: LoginResponse,
    val accessToken: String,
    val rememberMe: Boolean
)
