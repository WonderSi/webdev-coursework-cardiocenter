package com.medical.cardio.controller

import com.medical.cardio.dto.DashboardStatsResponse
import com.medical.cardio.service.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema

@Tag(name = "Аналитика", description = "Доступ DOCTOR и SUPER_ADMIN.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @Operation(summary = "Получить статистику обследований", description = "Статистика отдельно для city или region. Считаются записи обследований, не уникальные люди. Отсутствие диагноза не означает здоровье; прогнозы ML не рассчитываются.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Статистика", useReturnTypeSchema = true),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @GetMapping("/dashboard")
    fun getStats(
        @Parameter(description = "Модель обследований", schema = Schema(allowableValues = ["city", "region"]))
        @RequestParam(defaultValue = "city") modelKind: String
    ): ResponseEntity<DashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats(modelKind))
}
