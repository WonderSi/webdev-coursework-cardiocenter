package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema

data class DashboardStatsResponse(
    @field:Schema(description = "Selected observation model: city or region")
    val modelKind: String,
    @field:Schema(description = "Number of observations, not unique persons")
    val totalRecords: Long,
    val averageAge: Double?,
    val genderDistribution: List<DashboardCategory>,
    val ageGender: List<DashboardAgeGroup>,
    val bmi: List<DashboardCategory>,
    val alcohol: List<DashboardCategory>,
    val professions: List<DashboardCategory>,
    val districts: List<DashboardCategory>,
    val lungDisease: List<DashboardCategory>,
    val labCoverage: List<DashboardLabCoverage>,
    @field:Schema(description = "Observations with recorded diagnoses; other statuses are unknown")
    val diagnosedRecords: Long,
    val recordedDiagnoses: List<DashboardCategory>
)

data class DashboardCategory(val name: String, val count: Long, val percent: Double)
data class DashboardAgeGroup(val ageGroup: String, val maleCount: Long, val femaleCount: Long)
data class DashboardLabCoverage(
    val field: String,
    val label: String,
    val filledCount: Long,
    val missingCount: Long,
    val percent: Double
)

interface DiagnosisCountRaw {
    fun getName(): String
    fun getCount(): Long
}
