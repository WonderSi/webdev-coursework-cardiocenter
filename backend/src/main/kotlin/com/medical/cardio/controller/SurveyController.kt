package com.medical.cardio.controller

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.service.SurveyService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
    Контроллер опроса пациента.
    POST /api/survey/submit — принимает заполненные ответы, вызывает ML для прогноза, возвращает результат

    GET /api/survey/config — ЗАКОММЕНТИРОВАНО: конфигурация опроса перенесена на фронтенд (survey.config.ts).
    Если понадобится вернуть на бэкенд — раскомментировать код ниже.
*/
@RestController
@RequestMapping("/api/survey")
class SurveyController(
    private val surveyService: SurveyService,
    private val glossaryRepository: GlossaryRepository
) {

    @PostMapping("/submit")
    fun submitSurvey(@RequestBody @Valid request: SurveyRequest): ResponseEntity<SurveyResponse> =
        surveyService.submitSurvey(request).block()
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.internalServerError().build()
}
