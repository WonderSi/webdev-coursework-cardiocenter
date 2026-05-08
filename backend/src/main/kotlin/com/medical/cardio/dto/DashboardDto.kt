package com.medical.cardio.dto

data class DashboardStatsResponse(
    val totalPatients: Long,
    val genderDistribution: List<GenderStat>,
    val averageAge: Double?,
)

data class GenderStat(val label: String, val count: Long, val percent: Int)

interface GenderProjection {
    fun getLabel(): String
    fun getCount(): Long
}

interface DiagnosisProjection {
    fun getName(): String
    fun getCount(): Long
}