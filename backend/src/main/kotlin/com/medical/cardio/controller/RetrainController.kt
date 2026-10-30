package com.medical.cardio.controller

import com.medical.cardio.service.FullModelsResponse
import com.medical.cardio.service.RetrainResult
import com.medical.cardio.service.RetrainService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "ML-модели", description = "Доступ DOCTOR и SUPER_ADMIN. Операции требуют работающего ML-сервиса.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin")
class RetrainController(
    private val retrainService: RetrainService
) {
    @Operation(summary = "Получить метрики моделей", description = "Запрашивает у ML-сервиса версию моделей и метрики качества.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Метрики моделей", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
        ApiResponse(responseCode = "502", ref = "#/components/responses/MlServiceError")
    ])
    @GetMapping("/model")
    fun getModelStats(): ResponseEntity<FullModelsResponse> =
        ResponseEntity.ok(retrainService.getModelStats())

    @Operation(
        summary = "Переобучить модели",
        description = "Синхронно передаёт клинический датасет в ML-сервис и ожидает результат переобучения. Запрос изменяет модели."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Результат переобучения", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
        ApiResponse(responseCode = "502", ref = "#/components/responses/MlServiceError")
    ])
    @PostMapping("/retrain")
    fun retrain(): ResponseEntity<RetrainResult> =
        ResponseEntity.ok(retrainService.retrain())
}
