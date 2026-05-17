package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.DiagnosisEntity
import com.medical.cardio.entity.PatientEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.DiagnosisRepository
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.repository.GlossaryValueRepository
import com.medical.cardio.repository.PatientRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder

class DashboardServiceTest : BaseIntegrationTest() {

    @Autowired private lateinit var dashboardService: DashboardService
    @Autowired private lateinit var patientRepository: PatientRepository
    @Autowired private lateinit var diagnosisRepository: DiagnosisRepository
    @Autowired private lateinit var adminUserRepository: AdminUserRepository
    @Autowired private lateinit var glossaryRepository: GlossaryRepository
    @Autowired private lateinit var glossaryValueRepository: GlossaryValueRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    private var testAdminId: Long = 0
    private var maleGenderId: Long = 0
    private var femaleGenderId: Long = 0
    private var diagnosisValueId: Long = 0

    @BeforeEach
    fun setUp() {
        diagnosisRepository.deleteAll()
        patientRepository.deleteAll()

        testAdminId = adminUserRepository.save(
            AdminUserEntity(
                email = "dashboard-svc-test@cardio.ru",
                passwordHash = passwordEncoder.encode("TestPass@2000"),
                role = Role.DOCTOR
            )
        ).id

        val genderGlossary = glossaryRepository.findByName("gender")!!
        val genderValues = glossaryValueRepository.findAllByGlossaryId(genderGlossary.id)
        maleGenderId = genderValues.first { it.code == 1 }.id
        femaleGenderId = genderValues.first { it.code == 2 }.id

        val diagnosisGlossary = glossaryRepository.findByName("diagnosis")!!
        diagnosisValueId = glossaryValueRepository.findAllByGlossaryId(diagnosisGlossary.id).first().id
    }

    @AfterEach
    fun tearDown() {
        diagnosisRepository.deleteAll()
        patientRepository.deleteAll()
        adminUserRepository.deleteById(testAdminId)
    }

    private fun savePatient(genderId: Long, age: Int): PatientEntity =
        patientRepository.save(
            PatientEntity(
                creator = adminUserRepository.getReferenceById(testAdminId),
                gender = glossaryValueRepository.getReferenceById(genderId),
                age = age
            )
        )

    @Test
    fun `getStats with no patients returns zero totals and empty lists`() {
        val stats = dashboardService.getStats()

        assertEquals(0L, stats.totalPatients)
        assertTrue(stats.genderDistribution.isEmpty())
        assertNull(stats.averageAge)
        assertEquals(0, stats.genderDiagnoses.maleDiagnosed)
        assertEquals(0, stats.genderDiagnoses.malePercent)
        assertEquals(0, stats.genderDiagnoses.femaleDiagnosed)
        assertEquals(0, stats.genderDiagnoses.femalePercent)
        assertTrue(stats.topProfessions.isEmpty())
        assertTrue(stats.topDiagnoses.isEmpty())
        assertTrue(stats.districts.isEmpty())
    }

    @Test
    fun `getStats always returns fixed-length ageGender weightBmi and alcohol lists`() {
        val stats = dashboardService.getStats()

        assertEquals(6, stats.ageGender.size)
        assertEquals(4, stats.weightBmi.size)
        assertEquals(3, stats.alcohol.size)
    }

    @Test
    fun `getStats counts total patients correctly`() {
        savePatient(maleGenderId, 40)
        savePatient(maleGenderId, 50)
        savePatient(femaleGenderId, 35)

        val stats = dashboardService.getStats()

        assertEquals(3L, stats.totalPatients)
    }

    @Test
    fun `getStats gender distribution counts per gender`() {
        savePatient(maleGenderId, 40)
        savePatient(maleGenderId, 50)
        savePatient(femaleGenderId, 35)

        val stats = dashboardService.getStats()

        val maleStat = stats.genderDistribution.first { it.label == "Мужской" }
        val femaleStat = stats.genderDistribution.first { it.label == "Женский" }

        assertEquals(2L, maleStat.count)
        assertEquals(1L, femaleStat.count)
        assertEquals(66, maleStat.percent)
        assertEquals(33, femaleStat.percent)
    }

    @Test
    fun `getStats averageAge is computed correctly`() {
        savePatient(maleGenderId, 40)
        savePatient(femaleGenderId, 60)

        val stats = dashboardService.getStats()

        assertNotNull(stats.averageAge)
        assertEquals(50.0, stats.averageAge!!, 0.1)
    }

    @Test
    fun `getStats ageGender groups are in correct order`() {
        val stats = dashboardService.getStats()

        val expected = listOf("<30", "30-39", "40-49", "50-59", "60-69", "70+")
        assertEquals(expected, stats.ageGender.map { it.ageGroup })
    }

    @Test
    fun `getStats ageGender buckets patients into correct groups`() {
        savePatient(maleGenderId, 25)    // <30
        savePatient(maleGenderId, 35)    // 30-39
        savePatient(femaleGenderId, 75)  // 70+

        val stats = dashboardService.getStats()

        val under30 = stats.ageGender.first { it.ageGroup == "<30" }
        assertEquals(1, under30.healthyMale)
        assertEquals(0, under30.diagnosedMale)

        val thirties = stats.ageGender.first { it.ageGroup == "30-39" }
        assertEquals(1, thirties.healthyMale)

        val seventyPlus = stats.ageGender.first { it.ageGroup == "70+" }
        assertEquals(1, seventyPlus.healthyFemale)
    }

    @Test
    fun `getStats weightBmi categories are in correct order`() {
        val stats = dashboardService.getStats()

        val expected = listOf("Недостаточный", "Нормальный", "Избыточный", "Ожирение")
        assertEquals(expected, stats.weightBmi.map { it.category })
    }

    @Test
    fun `getStats alcohol categories are in correct order`() {
        val stats = dashboardService.getStats()

        val expected = listOf("Употребляю", "Употреблял ранее", "Никогда не употреблял")
        assertEquals(expected, stats.alcohol.map { it.category })
    }

    @Test
    fun `getStats genderDiagnoses distinguishes diagnosed from healthy`() {
        val male1 = savePatient(maleGenderId, 50)
        savePatient(maleGenderId, 55)
        savePatient(femaleGenderId, 45)

        diagnosisRepository.save(
            DiagnosisEntity(
                patient = patientRepository.getReferenceById(male1.id),
                diagnosis = glossaryValueRepository.getReferenceById(diagnosisValueId)
            )
        )

        val stats = dashboardService.getStats()

        assertEquals(1, stats.genderDiagnoses.maleDiagnosed)
        assertEquals(50, stats.genderDiagnoses.malePercent)
        assertEquals(0, stats.genderDiagnoses.femaleDiagnosed)
        assertEquals(0, stats.genderDiagnoses.femalePercent)
    }

    @Test
    fun `getStats topDiagnoses counts diagnoses correctly`() {
        val p1 = savePatient(maleGenderId, 50)
        val p2 = savePatient(femaleGenderId, 45)

        val diagRef = glossaryValueRepository.getReferenceById(diagnosisValueId)
        diagnosisRepository.save(DiagnosisEntity(patient = patientRepository.getReferenceById(p1.id), diagnosis = diagRef))
        diagnosisRepository.save(DiagnosisEntity(patient = patientRepository.getReferenceById(p2.id), diagnosis = diagRef))

        val stats = dashboardService.getStats()

        assertTrue(stats.topDiagnoses.isNotEmpty())
        val top = stats.topDiagnoses.first()
        assertEquals(2, top.diagnosedCount)
        assertEquals(100, top.percent)
    }
}
