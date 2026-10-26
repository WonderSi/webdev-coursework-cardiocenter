package com.medical.cardio.controller

import com.medical.cardio.dto.ResetPasswordRequest
import com.medical.cardio.dto.DoctorResponse
import com.medical.cardio.dto.CreateDoctorRequest
import com.medical.cardio.service.AdminService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping

@RestController
@RequestMapping("/api/admin/super-admin")
class AdminController(
    private val adminService: AdminService
) {

    @PostMapping("/users/{id}/reset-password")
    fun resetPassword(
        @PathVariable id: Long,
        @RequestBody @Valid request: ResetPasswordRequest
    ): ResponseEntity<Unit> {
        adminService.resetPassword(id, request.newPassword)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/doctors")
    fun createDoctor(
        @RequestBody @Valid request: CreateDoctorRequest): ResponseEntity<DoctorResponse> {
            val doctorResponse = adminService.createDoctor(request)
            return ResponseEntity.status(201).body(doctorResponse)
        }

    @GetMapping("/doctors")
    fun getDoctors(): ResponseEntity<List<DoctorResponse>> {
        val doctors = adminService.getDoctors()
        return ResponseEntity.ok(doctors)
    }
}
