package com.medical.cardio.service

import com.medical.cardio.dto.*
import com.medical.cardio.repository.DiagnosisRepository
import com.medical.cardio.repository.PatientRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardService(
    private val patientRepository: PatientRepository,
    private val diagnosisRepository: DiagnosisRepository
) {

    @Cacheable("dashboard-stats")
    @Transactional(readOnly = true)
    fun getStats(): DashboardStatsResponse {
        val total = patientRepository.countAll()
        val avgAge = patientRepository.averageAge()

        val genderStats = patientRepository.countByGender().map { row ->
            val percent = if (total > 0) (row.getCount() * 100L / total).toInt() else 0
            GenderStat(label = row.getLabel(), count = row.getCount(), percent = percent)
        }

        return DashboardStatsResponse(
            totalPatients    = total,
            genderDistribution = genderStats,
            averageAge       = avgAge,
            genderDiagnoses  = computeGenderDiagnoses(),
            ageGender        = computeAgeGender(),
            weightBmi        = computeWeightBmi(),
            alcohol          = computeAlcohol(),
            topProfessions   = computeTopProfessions(),
            topDiagnoses     = computeTopDiagnoses(),
            districts        = computeDistricts()
        )
    }

    private fun computeGenderDiagnoses(): GenderDiagnosisStat {
        val rows = patientRepository.countDiagnosedByGender()
        val male   = rows.find { it.getLabel() == "Мужской" }
        val female = rows.find { it.getLabel() == "Женский" }
        val mDiag  = male?.getDiagnosed()?.toInt() ?: 0
        val mTotal = male?.getTotal()?.toInt() ?: 0
        val fDiag  = female?.getDiagnosed()?.toInt() ?: 0
        val fTotal = female?.getTotal()?.toInt() ?: 0
        return GenderDiagnosisStat(
            maleDiagnosed   = mDiag,
            malePercent     = if (mTotal > 0) mDiag * 100 / mTotal else 0,
            femaleDiagnosed = fDiag,
            femalePercent   = if (fTotal > 0) fDiag * 100 / fTotal else 0
        )
    }

    private fun computeAgeGender(): List<AgeGenderStat> {
        val order = listOf("<30", "30-39", "40-49", "50-59", "60-69", "70+")
        val rows = patientRepository.countByAgeGroupAndGender()

        data class Key(val ageGroup: String, val gender: String, val diagnosed: Boolean)
        val map = rows.associate { Key(it.getAgeGroup(), it.getGender(), it.getIsDiagnosed()) to it.getCount() }

        return order.map { group ->
            AgeGenderStat(
                ageGroup        = group,
                healthyMale     = map[Key(group, "Мужской", false)]?.toInt() ?: 0,
                diagnosedMale   = map[Key(group, "Мужской", true)]?.toInt()  ?: 0,
                healthyFemale   = map[Key(group, "Женский", false)]?.toInt() ?: 0,
                diagnosedFemale = map[Key(group, "Женский", true)]?.toInt()  ?: 0
            )
        }
    }

    private fun computeWeightBmi(): List<BmiCategoryStat> {
        val order = listOf("Недостаточный", "Нормальный", "Избыточный", "Ожирение")
        val rows = patientRepository.countByBmiCategory()

        data class Key(val category: String, val diagnosed: Boolean)
        val map = rows.associate { Key(it.getCategory(), it.getIsDiagnosed()) to it.getCount() }

        return order.map { category ->
            val healthy   = map[Key(category, false)]?.toInt() ?: 0
            val diagnosed = map[Key(category, true)]?.toInt()  ?: 0
            val total     = healthy + diagnosed
            BmiCategoryStat(
                category        = category,
                healthyCount    = healthy,
                diagnosedCount  = diagnosed,
                diagnosedPercent = if (total > 0) diagnosed * 100 / total else 0
            )
        }
    }

    private fun computeAlcohol(): List<AlcoholStat> {
        val order = listOf("Употребляю", "Употреблял ранее", "Никогда не употреблял")
        val rows = patientRepository.countByAlcohol()

        data class Key(val category: String, val diagnosed: Boolean)
        val map = rows.associate { Key(it.getCategory(), it.getIsDiagnosed()) to it.getCount() }

        return order.map { category ->
            val healthy   = map[Key(category, false)]?.toInt() ?: 0
            val diagnosed = map[Key(category, true)]?.toInt()  ?: 0
            val total     = healthy + diagnosed
            AlcoholStat(
                category        = category,
                healthyCount    = healthy,
                diagnosedCount  = diagnosed,
                diagnosedPercent = if (total > 0) diagnosed * 100 / total else 0
            )
        }
    }

    private fun computeTopProfessions(): List<ProfessionStat> {
        return patientRepository.countDiagnosedByProfession(MIN_PROFESSION_SAMPLE)
            .take(5)
            .map { row ->
                val total     = row.getTotal().toInt()
                val diagnosed = row.getDiagnosedCount().toInt()
                ProfessionStat(
                    profName         = row.getProfName(),
                    diagnosedCount   = diagnosed,
                    diagnosedPercent = if (total > 0) diagnosed * 100 / total else 0
                )
            }
    }

    private fun computeTopDiagnoses(): List<DiagnosisStat> {
        val rows = diagnosisRepository.countByDiagnosis()
        val totalDiagnoses = rows.sumOf { it.getCount() }
        return rows.map { row ->
            DiagnosisStat(
                diagName       = row.getName(),
                diagnosedCount = row.getCount().toInt(),
                percent        = if (totalDiagnoses > 0) (row.getCount() * 100L / totalDiagnoses).toInt() else 0
            )
        }
    }

    private fun computeDistricts(): List<DistrictStat> {
        return patientRepository.countDiagnosedByRegion().map { row ->
            DistrictStat(name = row.getName(), numberOfDiagnoses = row.getDiagnosedCount().toInt())
        }
    }

    companion object {
        private const val MIN_PROFESSION_SAMPLE = 10
    }
}
