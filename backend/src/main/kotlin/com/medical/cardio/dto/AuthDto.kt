package com.medical.cardio.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Size(max = 50)
    val username: String,

    @field:NotBlank
    val password: String
)

data class LoginResponse(
    val token: String,
    val username: String,
    val role: String
)
