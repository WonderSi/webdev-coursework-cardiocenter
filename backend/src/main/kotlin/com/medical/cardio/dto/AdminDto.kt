package com.medical.cardio.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Email

data class ResetPasswordRequest(
    @field:NotBlank @field:Size(min = 12, max = 100)
    val newPassword: String
)

data class DoctorResponse(
    val id: Long,
    val email: String,
    val isBlocked: Boolean
)

data class CreateDoctorRequest(
    @field:NotBlank @field:Size(max = 255) @field:Email
    val email: String,
    @field:NotBlank @field:Size(min = 12, max = 100)
    val password: String
)

data class UpdateDoctorBlockedRequest(
    val isBlocked: Boolean
)