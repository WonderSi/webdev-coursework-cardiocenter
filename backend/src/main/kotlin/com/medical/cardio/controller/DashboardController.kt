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

@Tag(name = "Аналитика", description = "Доступ DOCTOR и SUPER_ADMIN.")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/admin")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @Operation(summary = "Получить статистику клинических данных", description = "Возвращает агрегированную статистику пациентов для Dashboard.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Статистика", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    ])
    @GetMapping("/dashboard")
    fun getStats(): ResponseEntity<DashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats())
}
