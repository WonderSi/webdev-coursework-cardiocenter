package com.medical.cardio.service

import com.medical.cardio.dto.*
import com.medical.cardio.entity.PatientEntity
import com.medical.cardio.repository.PatientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DashboardService(private val patientRepository: PatientRepository) {

    @Transactional(readOnly = true)
    fun getStats(): DashboardStatsResponse {
        val patients = patientRepository.findAllWithDiagnoses()
        val total = patients.size.toLong()

        val genderGroups = patients.groupBy { it.gender.value }
        val genderStats = genderGroups.map { (label, group) ->
            val percent = if (total > 0) (group.size * 100L / total).toInt() else 0
            GenderStat(label = label, count = group.size.toLong(), percent = percent)
        }

        val avgAge = if (patients.isEmpty()) null else patients.map { it.age }.average()

        return DashboardStatsResponse(
            totalPatients = total,
            genderDistribution = genderStats,
            averageAge = avgAge,
            genderDiagnoses = computeGenderDiagnoses(genderGroups),
            ageGender = computeAgeGender(patients),
            weightBmi = computeWeightBmi(patients),
            alcohol = computeAlcohol(patients),
            topProfessions = computeTopProfessions(patients),
            topDiagnoses = computeTopDiagnoses(patients),
            districts = computeDistricts(patients)
        )
    }

    private fun PatientEntity.isDiagnosed() = diagnoses.isNotEmpty()

    private fun ageGroup(age: Int) = when {
        age < 30 -> "<30"
        age < 40 -> "30-39"
        age < 50 -> "40-49"
        age < 60 -> "50-59"
        age < 70 -> "60-69"
        else -> "70+"
    }

    private fun bmiCategory(weight: BigDecimal?, height: BigDecimal?): String? {
        if (weight == null || height == null || height.compareTo(BigDecimal.ZERO) == 0) return null
        val heightM = height.divide(BigDecimal(100), 4, RoundingMode.HALF_UP)
        val bmi = weight.divide(heightM.multiply(heightM), 1, RoundingMode.HALF_UP)
        return when {
            bmi < BigDecimal("18.5") -> "Недостаточный"
            bmi < BigDecimal("25.0") -> "Нормальный"
            bmi < BigDecimal("30.0") -> "Избыточный"
            else -> "Ожирение"
        }
    }

    private fun computeGenderDiagnoses(genderGroups: Map<String, List<PatientEntity>>): GenderDiagnosisStat {
        val males = genderGroups["Мужской"] ?: emptyList()
        val females = genderGroups["Женский"] ?: emptyList()
        val maleDiagnosed = males.count { it.isDiagnosed() }
        val femaleDiagnosed = females.count { it.isDiagnosed() }
        return GenderDiagnosisStat(
            maleDiagnosed = maleDiagnosed,
            malePercent = if (males.isNotEmpty()) maleDiagnosed * 100 / males.size else 0,
            femaleDiagnosed = femaleDiagnosed,
            femalePercent = if (females.isNotEmpty()) femaleDiagnosed * 100 / females.size else 0
        )
    }

    private fun computeAgeGender(patients: List<PatientEntity>): List<AgeGenderStat> {
        val order = listOf("<30", "30-39", "40-49", "50-59", "60-69", "70+")
        data class Key(val ageGroup: String, val gender: String)
        val groups = patients.groupBy { Key(ageGroup(it.age), it.gender.value) }
        return order.map { group ->
            val males = groups[Key(group, "Мужской")] ?: emptyList()
            val females = groups[Key(group, "Женский")] ?: emptyList()
            AgeGenderStat(
                ageGroup = group,
                healthyMale = males.count { !it.isDiagnosed() },
                diagnosedMale = males.count { it.isDiagnosed() },
                healthyFemale = females.count { !it.isDiagnosed() },
                diagnosedFemale = females.count { it.isDiagnosed() }
            )
        }
    }

    private fun computeWeightBmi(patients: List<PatientEntity>): List<BmiCategoryStat> {
        val order = listOf("Недостаточный", "Нормальный", "Избыточный", "Ожирение")
        val groups = patients.groupBy { bmiCategory(it.weight, it.height) }
        return order.map { category ->
            val group = groups[category] ?: emptyList()
            val diagnosed = group.count { it.isDiagnosed() }
            BmiCategoryStat(
                category = category,
                healthyCount = group.count { !it.isDiagnosed() },
                diagnosedCount = diagnosed,
                diagnosedPercent = if (group.isNotEmpty()) diagnosed * 100 / group.size else 0
            )
        }
    }

    private fun computeAlcohol(patients: List<PatientEntity>): List<AlcoholStat> {
        val order = listOf("Употребляю", "Употреблял ранее", "Никогда не употреблял")
        val groups = patients.filter { it.alcohol != null }.groupBy { it.alcohol!!.value }
        return order.map { category ->
            val group = groups[category] ?: emptyList()
            val diagnosed = group.count { it.isDiagnosed() }
            AlcoholStat(
                category = category,
                healthyCount = group.count { !it.isDiagnosed() },
                diagnosedCount = diagnosed,
                diagnosedPercent = if (group.isNotEmpty()) diagnosed * 100 / group.size else 0
            )
        }
    }

    private fun computeTopProfessions(patients: List<PatientEntity>): List<ProfessionStat> {
        return patients.filter { it.profession != null }
            .groupBy { it.profession!!.value }
            .filter { it.value.size >= MIN_PROFESSION_SAMPLE }
            .map { (profName, group) ->
                val diagnosed = group.count { it.isDiagnosed() }
                ProfessionStat(
                    profName = profName,
                    diagnosedCount = diagnosed,
                    diagnosedPercent = if (group.isNotEmpty()) diagnosed * 100 / group.size else 0
                )
            }
            .sortedByDescending { it.diagnosedPercent }
            .take(5)
    }

    private fun computeTopDiagnoses(patients: List<PatientEntity>): List<DiagnosisStat> {
        val allDiagnoses = patients.flatMap { p -> p.diagnoses.map { d -> d.diagnosis.value } }
        val totalDiagnoses = allDiagnoses.size
        return allDiagnoses.groupBy { it }
            .map { (name, list) ->
                DiagnosisStat(
                    diagName = name,
                    diagnosedCount = list.size,
                    percent = if (totalDiagnoses > 0) list.size * 100 / totalDiagnoses else 0
                )
            }
            .sortedByDescending { it.diagnosedCount }
    }

    private fun computeDistricts(patients: List<PatientEntity>): List<DistrictStat> {
        return patients.filter { it.region != null }
            .groupBy { it.region!!.value }
            .map { (name, group) ->
                DistrictStat(name = name, numberOfDiagnoses = group.count { it.isDiagnosed() })
            }
            .sortedBy { it.name }
    }

    companion object {
        private const val MIN_PROFESSION_SAMPLE = 10
    }
}
