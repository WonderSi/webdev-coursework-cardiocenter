package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Email

data class ResetPasswordRequest(
    @field:NotBlank @field:Size(min = 12, max = 100)
    @field:Schema(description = "Новый пароль: от 12 до 100 символов", example = "NewExamplePassword123!", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    val newPassword: String
)

data class DoctorResponse(
    @field:Schema(description = "Идентификатор записи", example = "1")
    val id: Long,
    @field:Schema(description = "Email сотрудника", example = "doctor@example.com")
    val email: String,
    @field:Schema(description = "true — врач заблокирован; false — активен", example = "false")
    val isBlocked: Boolean
)

data class CreateDoctorRequest(
    @field:NotBlank @field:Size(max = 255) @field:Email
    @field:Schema(description = "Email сотрудника", example = "doctor@example.com")
    val email: String,
    @field:NotBlank @field:Size(min = 12, max = 100)
    @field:Schema(description = "Пароль сотрудника", example = "ExamplePassword123!", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    val password: String
)

data class UpdateDoctorBlockedRequest(
    @field:Schema(description = "true — врач заблокирован; false — активен", example = "false")
    val isBlocked: Boolean
)
