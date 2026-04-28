package com.medical.cardio.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Email @field:Size(max = 255)
    val email: String,

    @field:NotBlank
    val password: String
)

data class LoginResponse(
    val email: String,
    val role: String
)
