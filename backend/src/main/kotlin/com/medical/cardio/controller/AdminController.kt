package com.medical.cardio.controller

import com.medical.cardio.dto.CreateDoctorRequest
import com.medical.cardio.dto.DoctorResponse
import com.medical.cardio.dto.ResetPasswordRequest
import com.medical.cardio.dto.UpdateDoctorBlockedRequest
import com.medical.cardio.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Управление врачами", description = "Доступ только SUPER_ADMIN.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin/super-admin")
class AdminController(
    private val adminService: AdminService
) {

    @Operation(
        summary = "Установить новый пароль пользователя",
        description = "SUPER_ADMIN задаёт новый пароль по ID пользователя. Действующие JWT этим запросом не отзываются."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Пароль изменён, тело ответа отсутствует", content = [Content()]),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @PostMapping("/users/{id}/reset-password")
    fun resetPassword(
        @Parameter(description = "ID учётной записи", example = "1") @PathVariable id: Long,
        @RequestBody @Valid request: ResetPasswordRequest
    ): ResponseEntity<Unit> {
        adminService.resetPassword(id, request.newPassword)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "Создать учётную запись врача",
        description = "Создаёт активного DOCTOR с уникальным email. Пароль хранится в виде BCrypt-хэша."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Врач создан", useReturnTypeSchema = true),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @PostMapping("/doctors")
    fun createDoctor(
        @RequestBody @Valid request: CreateDoctorRequest
    ): ResponseEntity<DoctorResponse> {
        val doctorResponse = adminService.createDoctor(request)
        return ResponseEntity.status(201).body(doctorResponse)
    }

    @Operation(
        summary = "Получить список врачей",
        description = "Возвращает всех DOCTOR, включая заблокированных. При отсутствии врачей возвращается []."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Список врачей", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @GetMapping("/doctors")
    fun getDoctors(): ResponseEntity<List<DoctorResponse>> {
        val doctors = adminService.getDoctors()
        return ResponseEntity.ok(doctors)
    }

    @Operation(
        summary = "Заблокировать или разблокировать врача",
        description = "isBlocked=true блокирует, false разблокирует. Изменять SUPER_ADMIN запрещено. Блокировка запрещает вход и запросы с ранее выданным JWT."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Состояние обновлено, тело ответа отсутствует", content = [Content()]),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @PutMapping("/doctors/{id}/blocked")
    fun updateDoctorBlocked(
        @Parameter(description = "ID учётной записи", example = "1") @PathVariable id: Long,
        @RequestBody @Valid request: UpdateDoctorBlockedRequest
    ): ResponseEntity<Unit> {
        adminService.updateDoctorBlocked(id, request.isBlocked)
        return ResponseEntity.noContent().build()
    }
}
