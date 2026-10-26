package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Email @field:Size(max = 255)
    @field:Schema(description = "Email сотрудника", example = "doctor@example.com")
    val email: String,

    @field:NotBlank
    @field:Schema(description = "Пароль сотрудника", example = "ExamplePassword123!", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    val password: String,

    @field:Schema(description = "Сохранить cookie на 30 дней; срок JWT не меняется", example = "false")
    val rememberMe: Boolean = false
)

data class LoginResponse(
    @field:Schema(description = "Email сотрудника", example = "doctor@example.com")
    val email: String,
    @field:Schema(description = "Роль сотрудника с префиксом ROLE_", example = "ROLE_DOCTOR")
    val role: String
)
