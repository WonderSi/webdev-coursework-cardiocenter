package com.medical.cardio.controller

import com.medical.cardio.dto.UploadReport
import com.medical.cardio.service.UploadService
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

@RestController
@RequestMapping("/api/admin/upload")
class UploadController(
    private val uploadService: UploadService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<UploadReport> {
        validateFile(file)
        val email = SecurityContextHolder.getContext().authentication.name
        return ResponseEntity.ok(uploadService.upload(file, email))
    }

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
