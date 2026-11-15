package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.DiagnosisEntity
import com.medical.cardio.entity.PatientEntity
import com.medical.cardio.repository.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import kotlin.test.*

class DashboardServiceTest : BaseIntegrationTest() {
    @Autowired private lateinit var dashboard: DashboardService
    @Autowired private lateinit var patients: PatientRepository
    @Autowired private lateinit var diagnoses: DiagnosisRepository
    @Autowired private lateinit var users: AdminUserRepository
    @Autowired private lateinit var glossaries: GlossaryRepository
    @Autowired private lateinit var values: GlossaryValueRepository
    @Autowired private lateinit var cache: CacheManager
    private var userId = 0L

    @BeforeEach
    fun setUp() {
        diagnoses.deleteAll()
        patients.deleteAll()
        cache.getCache("dashboard-stats")?.clear()
        userId = users.save(AdminUserEntity(email = "dashboard-test@test.invalid", passwordHash = "unused-test-hash")).id
    }

    @AfterEach
    fun cleanUp() {
        diagnoses.deleteAll()
        patients.deleteAll()
        users.deleteById(userId)
        cache.getCache("dashboard-stats")?.clear()
    }

    private fun glossary(name: String, code: Int) = values.findAllByGlossaryId(
        requireNotNull(glossaries.findByName(name)).id).first { it.code == code }

    private fun patient(kind: String = "city", age: Int = 40, gender: Int = 1,
                        height: String? = "175", weight: String? = "80", lung: Int = 0,
                        glucose: String? = null, profession: Int? = null): PatientEntity =
        patients.saveAndFlush(PatientEntity(
            creator = users.getReferenceById(userId), gender = glossary("gender", gender), age = age,
            modelKind = kind, modelYear = if (kind == "city") 2021 else null,
            chronicLungDisease = lung, height = height?.toBigDecimal(), weight = weight?.toBigDecimal(),
            glucose = glucose?.toBigDecimal(), profession = profession?.let { glossary("profession", it) },
            alcohol = glossary("alcohol", 1), region = glossary("region", 1)
        ))

    @Test
    fun `empty model has zero counts and no invented diagnosis status`() {
        val stats = dashboard.getStats("city")
        assertEquals(0L, stats.totalRecords)
        assertNull(stats.averageAge)
        assertEquals(0L, stats.diagnosedRecords)
        assertTrue(stats.recordedDiagnoses.isEmpty())
        assertTrue(stats.labCoverage.all { it.filledCount == 0L && it.missingCount == 0L && it.percent == 0.0 })
        assertEquals(6, stats.ageGender.size)
        assertTrue(stats.bmi.all { it.count == 0L })
    }

    @Test
    fun `all charts and cache keys isolate city from region`() {
        patient(age = 30, lung = 1, profession = 1)
        patient(age = 50, gender = 2, profession = 2)
        patient(kind = "region", age = 90, profession = 13)
        val city = dashboard.getStats("city")
        val region = dashboard.getStats("region")
        assertEquals(2L, city.totalRecords)
        assertEquals(1L, region.totalRecords)
        assertEquals(40.0, city.averageAge)
        assertEquals(90.0, region.averageAge)
        assertEquals(2L, city.genderDistribution.sumOf { it.count })
        assertEquals(2L, city.ageGender.sumOf { it.maleCount + it.femaleCount })
        for (categories in listOf(city.bmi, city.alcohol, city.professions, city.districts, city.lungDisease)) {
            assertEquals(2L, categories.sumOf { it.count })
        }
        assertEquals(1L, city.lungDisease.first { it.name == "Да" }.count)
        assertEquals(50.0, city.lungDisease.first { it.name == "Да" }.percent)
        assertEquals(0L, region.lungDisease.first { it.name == "Да" }.count)
        assertEquals(2L, dashboard.getStats("city").totalRecords)
    }

    @Test
    fun `lab completeness treats zero as filled and null as missing`() {
        patient(glucose = "0")
        patient(glucose = "5.2")
        patient(glucose = null)
        patient(kind = "region", glucose = "9")
        val stats = dashboard.getStats("city")
        val glucose = stats.labCoverage.first { it.field == "glucose" }
        assertEquals(2L, glucose.filledCount)
        assertEquals(1L, glucose.missingCount)
        assertEquals(66.7, glucose.percent)
        val cholesterol = stats.labCoverage.first { it.field == "cholesterol" }
        assertEquals(0L, cholesterol.filledCount)
        assertEquals(3L, cholesterol.missingCount)
    }

    @Test
    fun `BMI missing or zero measurements are unknown not obese`() {
        patient(height = null)
        patient(height = "0")
        patient(weight = null)
        patient(height = "180", weight = "70")
        val stats = dashboard.getStats()
        assertEquals(3L, stats.bmi.first { it.name == "Нет данных" }.count)
        assertEquals(1L, stats.bmi.first { it.name == "Нормальный" }.count)
        assertEquals(0L, stats.bmi.first { it.name == "Ожирение" }.count)
    }

    @Test
    fun `professions include small groups and missing values without ranking by disease`() {
        patient(profession = 1)
        patient(profession = 13)
        patient(profession = null)
        val stats = dashboard.getStats()
        assertEquals(3, stats.professions.size)
        assertEquals(1L, stats.professions.first { it.name == "Нет данных" }.count)
        assertEquals(33.3, stats.professions.first { it.name == "Другое" }.percent)
    }

    @Test
    fun `diagnosis counts only recorded positives and respects model filter`() {
        val city = patient()
        patient()
        val region = patient(kind = "region")
        val diagnosis = glossary("diagnosis", 1)
        repeat(2) { diagnoses.saveAndFlush(DiagnosisEntity(patient = city, diagnosis = diagnosis)) }
        diagnoses.saveAndFlush(DiagnosisEntity(patient = region, diagnosis = diagnosis))
        val stats = dashboard.getStats("city")
        assertEquals(2L, stats.totalRecords)
        assertEquals(1L, stats.diagnosedRecords)
        assertEquals(1L, stats.recordedDiagnoses.single().count)
        assertEquals(50.0, stats.recordedDiagnoses.single().percent)
    }

    @Test
    fun `age group boundaries count observations exactly once`() {
        for (age in listOf(29, 30, 39, 40, 49, 50, 59, 60, 69, 70)) patient(age = age)
        assertEquals(listOf(1L, 2L, 2L, 2L, 2L, 1L), dashboard.getStats().ageGender.map { it.maleCount })
    }

    @Test
    fun `unknown model is rejected`() {
        assertFailsWith<IllegalArgumentException> { dashboard.getStats("other") }
    }
}
