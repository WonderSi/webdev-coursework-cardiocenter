package com.medical.cardio.controller

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.service.SurveyService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
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
        description = "Валидирует ответы и передаёт их ML-сервису. Поля genderId, alcoholId, professionId и regionId передаются как числовые коды, используемые ML-сервисом. При ошибке ML текущая реализация возвращает 200 с пустым predictions. Вероятности в ответе выражены целыми процентами от 0 до 100."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Результат прогнозирования", useReturnTypeSchema = true),
        ApiResponse(
            responseCode = "400",
            description = "Ошибки валидации или некорректные данные",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(type = "object"),
                    examples = [
                        ExampleObject(
                            name = "validationError",
                            value = """{"errors":{"age":"must be less than or equal to 120"}}"""
                        ),
                        ExampleObject(
                            name = "badRequest",
                            value = """{"error":"Некорректный запрос"}"""
                        )
                    ]
                )
            ]
        ),
        ApiResponse(responseCode = "500", description = "Ошибка обращения к ML-сервису или отсутствие результата", content = [Content()])
    ])
    @PostMapping("/submit")
    fun submitSurvey(@RequestBody @Valid request: SurveyRequest): ResponseEntity<SurveyResponse> =
        surveyService.submitSurvey(request).block()
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.internalServerError().build()
}
