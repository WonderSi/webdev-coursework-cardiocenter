package com.medical.cardio.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank @field:Size(min = 12, max = 100)
    val newPassword: String
)
