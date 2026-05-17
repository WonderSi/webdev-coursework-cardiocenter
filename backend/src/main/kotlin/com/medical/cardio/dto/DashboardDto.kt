package com.medical.cardio.dto

data class DashboardStatsResponse(
    val totalPatients: Long,
    val genderDistribution: List<GenderStat>,
    val averageAge: Double?,
    val genderDiagnoses: GenderDiagnosisStat,
    val ageGender: List<AgeGenderStat>,
    val weightBmi: List<BmiCategoryStat>,
    val alcohol: List<AlcoholStat>,
    val topProfessions: List<ProfessionStat>,
    val topDiagnoses: List<DiagnosisStat>,
    val districts: List<DistrictStat>
)

data class GenderStat(val label: String, val count: Long, val percent: Int)

data class GenderDiagnosisStat(
    val maleDiagnosed: Int,
    val malePercent: Int,
    val femaleDiagnosed: Int,
    val femalePercent: Int
)

data class AgeGenderStat(
    val ageGroup: String,
    val healthyMale: Int,
    val diagnosedMale: Int,
    val healthyFemale: Int,
    val diagnosedFemale: Int
)

data class BmiCategoryStat(
    val category: String,
    val healthyCount: Int,
    val diagnosedCount: Int,
    val diagnosedPercent: Int
)

data class AlcoholStat(
    val category: String,
    val healthyCount: Int,
    val diagnosedCount: Int,
    val diagnosedPercent: Int
)

data class ProfessionStat(
    val profName: String,
    val diagnosedCount: Int,
    val diagnosedPercent: Int
)

data class DiagnosisStat(
    val diagName: String,
    val diagnosedCount: Int,
    val percent: Int
)

data class DistrictStat(
    val name: String,
    val numberOfDiagnoses: Int
)

interface GenderProjection {
    fun getLabel(): String
    fun getCount(): Long
}

interface DiagnosisProjection {
    fun getName(): String
    fun getCount(): Long
}
