package com.medical.cardio.controller

import com.medical.cardio.dto.ResetPasswordRequest
import com.medical.cardio.service.AdminService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/extended")
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
}
