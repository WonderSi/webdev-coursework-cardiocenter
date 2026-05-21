package com.medical.cardio.controller

import com.medical.cardio.service.RetrainResult
import com.medical.cardio.service.RetrainService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class RetrainController(
    private val retrainService: RetrainService
) {
    @PostMapping("/retrain")
    fun retrain(): ResponseEntity<RetrainResult> =
        ResponseEntity.ok(retrainService.retrain())
}
