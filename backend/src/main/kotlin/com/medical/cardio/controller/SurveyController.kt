package com.medical.cardio.controller

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.service.SurveyService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/survey/submit")
class SurveyController(
    private val surveyService: SurveyService
) {

    @PostMapping
    fun submitSurvey(@RequestBody @Valid request: SurveyRequest): ResponseEntity<SurveyResponse> =
        surveyService.submitSurvey(request).block()
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.internalServerError().build()
}
