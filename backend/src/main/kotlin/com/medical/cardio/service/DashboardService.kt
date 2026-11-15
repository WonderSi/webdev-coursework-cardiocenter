package com.medical.cardio.service

import com.medical.cardio.dto.DashboardAgeGroup
import com.medical.cardio.dto.DashboardCategory
import com.medical.cardio.dto.DashboardLabCoverage
import com.medical.cardio.dto.DashboardStatsResponse
import com.medical.cardio.repository.DashboardCount
import com.medical.cardio.repository.DashboardRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import kotlin.math.round

@Service
class DashboardService(private val dashboardRepository: DashboardRepository) {
    @Cacheable(value = ["dashboard-stats"], key = "'observations-v2:' + #modelKind")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    fun getStats(modelKind: String = "city"): DashboardStatsResponse {
        require(modelKind in setOf("city", "region")) { "modelKind должен быть city или region" }
        val summary = dashboardRepository.summary(modelKind)
        val total = (summary.getValue("total") as Number).toLong()
        val ages = dashboardRepository.byAgeAndGender(modelKind)
        val labs = dashboardRepository.labCoverage(modelKind)
        return DashboardStatsResponse(
            modelKind = modelKind,
            totalRecords = total,
            averageAge = (summary["average_age"] as? Number)?.toDouble(),
            genderDistribution = categories(dashboardRepository.byGender(modelKind), total),
            ageGender = listOf("<30", "30–39", "40–49", "50–59", "60–69", "70+").map { group ->
                DashboardAgeGroup(group,
                    ages.filter { it.ageGroup == group && it.gender == "Мужской" }.sumOf { it.count },
                    ages.filter { it.ageGroup == group && it.gender == "Женский" }.sumOf { it.count })
            },
            bmi = categories(dashboardRepository.byBmi(modelKind), total,
                listOf("Недостаточный", "Нормальный", "Избыточный", "Ожирение", "Нет данных")),
            alcohol = categories(dashboardRepository.byAlcohol(modelKind), total),
            professions = categories(dashboardRepository.byProfession(modelKind), total),
            districts = categories(dashboardRepository.byDistrict(modelKind), total),
            lungDisease = categories(dashboardRepository.byLungDisease(modelKind), total,
                listOf("Да", "Нет", "Нет данных")),
            labCoverage = linkedMapOf(
                "glucose" to "Глюкоза", "cholesterol" to "Холестерин",
                "hdl_cholesterol" to "Холестерин ЛПВП", "ldl_cholesterol" to "Холестерин ЛПНП",
                "triglycerides" to "Триглицериды"
            ).map { (field, label) ->
                val filled = (labs.getValue(field) as Number).toLong()
                DashboardLabCoverage(field, label, filled, total - filled, percent(filled, total))
            },
            diagnosedRecords = (summary.getValue("diagnosed") as Number).toLong(),
            recordedDiagnoses = categories(dashboardRepository.recordedDiagnoses(modelKind), total)
        )
    }

    private fun categories(rows: List<DashboardCount>, total: Long, order: List<String>? = null): List<DashboardCategory> {
        val counts = rows.associate { it.name to it.count }
        return (order ?: rows.map { it.name }).map { name ->
            val count = counts[name] ?: 0L
            DashboardCategory(name, count, percent(count, total))
        }
    }

    private fun percent(count: Long, total: Long): Double =
        if (total == 0L) 0.0 else round(count.toDouble() * 1000 / total) / 10
}
