package com.medical.cardio.controller

import com.medical.cardio.dto.PatientManualCreateRequest
import com.medical.cardio.service.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Клинические данные", description = "Работа с клиническим датасетом. Доступ DOCTOR и SUPER_ADMIN.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin/patients")
class PatientController(
    private val uploadService: UploadService
) {
    @Operation(
        summary = "Добавить пациента вручную",
        description = "Сохраняет клиническую запись и диагнозы от имени текущего сотрудника. Поля справочников содержат коды, а не ID записей справочника."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Пациент сохранён, тело ответа отсутствует", content = [Content()]),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @PostMapping
    fun createPatient(@RequestBody request: PatientManualCreateRequest): ResponseEntity<Void> {
        val email = SecurityContextHolder.getContext().authentication.name
        uploadService.savePatient(request, email)
        return ResponseEntity.ok().build()
    }
}
