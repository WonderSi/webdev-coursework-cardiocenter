package com.medical.cardio.controller

import com.medical.cardio.dto.DashboardStatsResponse
import com.medical.cardio.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/dashboard")
    fun getStats(): ResponseEntity<DashboardStatsResponse> =
        ResponseEntity.ok(dashboardService.getStats())
}
