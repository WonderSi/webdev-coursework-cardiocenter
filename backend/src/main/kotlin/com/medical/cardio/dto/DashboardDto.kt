package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema

data class DashboardStatsResponse(
    @field:Schema(description = "Общее количество пациентов")
    val totalPatients: Long,
    @field:Schema(description = "Распределение по полу")
    val genderDistribution: List<GenderStat>,
    @field:Schema(description = "Средний возраст; null при отсутствии данных")
    val averageAge: Double?,
    @field:Schema(description = "Статистика диагнозов по полу")
    val genderDiagnoses: GenderDiagnosisStat,
    @field:Schema(description = "Статистика по возрастным группам и полу")
    val ageGender: List<AgeGenderStat>,
    @field:Schema(description = "Распределение по категориям ИМТ")
    val weightBmi: List<BmiCategoryStat>,
    @field:Schema(description = "Статистика употребления алкоголя")
    val alcohol: List<AlcoholStat>,
    @field:Schema(description = "Статистика по профессиям")
    val topProfessions: List<ProfessionStat>,
    @field:Schema(description = "Статистика диагнозов")
    val topDiagnoses: List<DiagnosisStat>,
    @field:Schema(description = "Статистика по районам")
    val districts: List<DistrictStat>
)

data class GenderStat(val label: String, val count: Long, val percent: Int)

data class GenderDiagnosisStat(
    @field:Schema(description = "Количество мужчин с диагнозами")
    val maleDiagnosed: Int,
    @field:Schema(description = "Доля мужчин с диагнозами, проценты")
    val malePercent: Int,
    @field:Schema(description = "Количество женщин с диагнозами")
    val femaleDiagnosed: Int,
    @field:Schema(description = "Доля женщин с диагнозами, проценты")
    val femalePercent: Int
)

data class AgeGenderStat(
    @field:Schema(description = "Возрастная группа")
    val ageGroup: String,
    @field:Schema(description = "Мужчины без диагнозов")
    val healthyMale: Int,
    @field:Schema(description = "Мужчины с диагнозами")
    val diagnosedMale: Int,
    @field:Schema(description = "Женщины без диагнозов")
    val healthyFemale: Int,
    @field:Schema(description = "Женщины с диагнозами")
    val diagnosedFemale: Int
)

data class BmiCategoryStat(
    @field:Schema(description = "Категория группировки")
    val category: String,
    @field:Schema(description = "Количество пациентов без диагнозов")
    val healthyCount: Int,
    @field:Schema(description = "Количество пациентов с диагнозами")
    val diagnosedCount: Int,
    @field:Schema(description = "Доля пациентов с диагнозами, проценты")
    val diagnosedPercent: Int
)

data class AlcoholStat(
    @field:Schema(description = "Категория группировки")
    val category: String,
    @field:Schema(description = "Количество пациентов без диагнозов")
    val healthyCount: Int,
    @field:Schema(description = "Количество пациентов с диагнозами")
    val diagnosedCount: Int,
    @field:Schema(description = "Доля пациентов с диагнозами, проценты")
    val diagnosedPercent: Int
)

data class ProfessionStat(
    @field:Schema(description = "Название профессии")
    val profName: String,
    @field:Schema(description = "Количество пациентов с диагнозами")
    val diagnosedCount: Int,
    @field:Schema(description = "Доля пациентов с диагнозами, проценты")
    val diagnosedPercent: Int
)

data class DiagnosisStat(
    @field:Schema(description = "Название диагноза")
    val diagName: String,
    @field:Schema(description = "Количество пациентов с диагнозами")
    val diagnosedCount: Int,
    @field:Schema(description = "Доля, проценты")
    val percent: Int
)

data class DistrictStat(
    @field:Schema(description = "Название района")
    val name: String,
    @field:Schema(description = "Количество диагнозов")
    val numberOfDiagnoses: Int
)

interface GenderProjection {
    fun getLabel(): String
    fun getCount(): Long
}

interface GenderDiagnosisRaw {
    fun getLabel(): String
    fun getTotal(): Long
    fun getDiagnosed(): Long
}

interface AgeGenderRaw {
    fun getAgeGroup(): String
    fun getGender(): String
    fun getIsDiagnosed(): Boolean
    fun getCount(): Long
}

interface CategoryDiagnosisRaw {
    fun getCategory(): String
    fun getIsDiagnosed(): Boolean
    fun getCount(): Long
}

interface ProfessionRaw {
    fun getProfName(): String
    fun getTotal(): Long
    fun getDiagnosedCount(): Long
}

interface DiagnosisCountRaw {
    fun getName(): String
    fun getCount(): Long
}

interface DistrictRaw {
    fun getName(): String
    fun getDiagnosedCount(): Long
}
