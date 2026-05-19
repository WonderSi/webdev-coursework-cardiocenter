package com.medical.cardio.controller

import com.medical.cardio.dto.PatientManualCreateRequest
import com.medical.cardio.service.UploadService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/patients")
class PatientController(
    private val uploadService: UploadService
) {
    @PostMapping
    fun createPatient(@RequestBody request: PatientManualCreateRequest): ResponseEntity<Void> {
        val email = SecurityContextHolder.getContext().authentication.name
        uploadService.savePatient(request, email)
        return ResponseEntity.ok().build()
    }
}
