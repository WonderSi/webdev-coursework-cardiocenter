package com.medical.cardio.controller

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.service.SurveyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Публичный опрос", description = "Доступ без регистрации. Ответы используются для разового прогноза и не добавляются в клинический датасет.")
@RestController
@RequestMapping("/api/survey")
class SurveyController(
    private val surveyService: SurveyService,
    private val glossaryRepository: GlossaryRepository
) {

    @Operation(
        summary = "Получить прогноз по ответам опроса",
        description = "Прогноз явно выбранной модели: city — отдельное обследование, region — сводные данные. Район проживания не переключает модель. Для city обязателен период экологических данных year: 2013, 2017, 2019 или 2021; для region year должен быть null. chronicLungDisease обязателен (0/1). professionId — код 1–13 справочника profession по notebook-13-v1 из ML-контракта; alcoholId 1–3 преобразуется в ML-код 0–2. Прочие Id — коды, не ID записей БД. Ответ содержит один составной исход, целый процент, класс prediction и modelVersion. Ошибка ML даёт 502 Problem Details."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Результат прогнозирования", useReturnTypeSchema = true),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "502", ref = "#/components/responses/MlServiceError")
    ])
    @PostMapping("/submit")
    fun submitSurvey(@RequestBody @Valid request: SurveyRequest): ResponseEntity<SurveyResponse> =
        surveyService.submitSurvey(request).block()
            ?.let { ResponseEntity.ok(it) }
            ?: throw MlServiceException(IllegalStateException("Empty survey response"))
}
