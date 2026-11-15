package com.medical.cardio.dto

import com.medical.cardio.entity.AdminSection
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
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
    val isBlocked: Boolean,
    @field:Schema(description = "Разрешённые разделы врача")
    val allowedSections: Set<AdminSection>
)

data class CreateDoctorRequest(
    @field:NotBlank @field:Size(max = 255) @field:Email
    @field:Schema(description = "Email сотрудника", example = "doctor@example.com")
    val email: String,
    @field:NotBlank @field:Size(min = 12, max = 100)
    @field:Schema(description = "Пароль сотрудника", example = "ExamplePassword123!", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
    val password: String,
    @field:Schema(description = "Разрешённые разделы. Если поле пропущено, список пуст.")
    @field:JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    val allowedSections: Set<AdminSection> = emptySet()
)

data class UpdateDoctorSectionsRequest(
    @field:Schema(description = "Полный новый набор разделов. Пустой список удаляет все разрешения.")
    @field:JsonSetter(nulls = Nulls.FAIL, contentNulls = Nulls.FAIL)
    val allowedSections: Set<AdminSection>
)

data class UpdateDoctorBlockedRequest(
    @field:Schema(description = "true — врач заблокирован; false — активен", example = "false")
    val isBlocked: Boolean
)
