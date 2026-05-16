package com.medical.cardio.service

import com.medical.cardio.dto.DashboardStatsResponse
import com.medical.cardio.dto.GenderStat
import com.medical.cardio.repository.PatientRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardService(
    private val patientRepository: PatientRepository
) {

    @Cacheable("dashboard-stats")
    @Transactional(readOnly = true)
    fun getStats(): DashboardStatsResponse {
        val total = patientRepository.countAll()
        val genderRaw = patientRepository.countByGender()
        val avgAge = patientRepository.averageAge()

        val genderStats = genderRaw.map { g ->
            val percent = if (total > 0) ((g.getCount() * 100) / total).toInt() else 0
            GenderStat(label = g.getLabel(), count = g.getCount(), percent = percent)
        }

        return DashboardStatsResponse(
            totalPatients = total,
            genderDistribution = genderStats,
            averageAge = avgAge
        )
    }
}
