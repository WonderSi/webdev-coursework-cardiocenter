package com.medical.cardio.controller

import com.medical.cardio.dto.UploadReport
import com.medical.cardio.service.UploadService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Импорт данных", description = "Загрузка клинического датасета. Доступ DOCTOR и SUPER_ADMIN.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin/upload")
class UploadController(
    private val uploadService: UploadService
) {

    @Operation(
        summary = "Импортировать пациентов из файла",
        description = "Принимает multipart/form-data с полем file: CSV, XLS или XLSX, до 10 МБ. Некорректные строки пропускаются и перечисляются в отчёте; успешный HTTP-ответ не означает, что сохранены все строки."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Отчёт импорта", useReturnTypeSchema = true),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "413", ref = "#/components/responses/PayloadTooLarge"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@Parameter(description = "Файл CSV, XLS или XLSX, до 10 МБ", required = true) @RequestParam("file") file: MultipartFile): ResponseEntity<UploadReport> {
        validateFile(file)
        val email = SecurityContextHolder.getContext().authentication.name
        return ResponseEntity.ok(uploadService.upload(file, email))
    }

    @Operation(summary = "Скачать шаблон импорта", description = "Возвращает файл patients_template.xlsx с заголовками для загрузки пациентов.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Файл XLSX",
            content = [
                Content(
                    mediaType = "application/octet-stream",
                    schema = Schema(type = "string", format = "binary")
                )
            ],
            headers = [
                Header(
                    name = "Content-Disposition",
                    description = "Имя файла для скачивания",
                    schema = Schema(type = "string")
                )
            ]
        ),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @GetMapping("/template")
    fun downloadTemplate(): ResponseEntity<ByteArray> {
        val bytes = uploadService.generateTemplate()
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"patients_template.xlsx\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) throw IllegalArgumentException("Файл пустой")
        val ext = file.originalFilename?.substringAfterLast('.')?.lowercase()
        if (ext !in setOf("xlsx", "xls", "csv"))
            throw IllegalArgumentException("Допустимые форматы: .xlsx, .xls, .csv")
    }
}
