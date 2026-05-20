package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get

class DashboardControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "dashboard-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
    }

    @AfterEach
    fun tearDown() {
        adminUserRepository.delete(testUser)
    }

    @Test
    fun `getStats with DOCTOR token returns 200`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/dashboard") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalPatients") { exists() }
        }
    }

    @Test
    fun `getStats without token returns 401`() {
        mockMvc.get("/api/admin/dashboard").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getStats response contains all expected top-level fields`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/dashboard") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalPatients") { exists() }
            jsonPath("$.genderDistribution") { exists() }
            jsonPath("$.genderDiagnoses") { exists() }
            jsonPath("$.genderDiagnoses.maleDiagnosed") { exists() }
            jsonPath("$.genderDiagnoses.femaleDiagnosed") { exists() }
            jsonPath("$.ageGender") { isArray() }
            jsonPath("$.weightBmi") { isArray() }
            jsonPath("$.alcohol") { isArray() }
            jsonPath("$.topProfessions") { isArray() }
            jsonPath("$.topDiagnoses") { isArray() }
            jsonPath("$.districts") { isArray() }
        }
    }

    @Test
    fun `getStats with DOCTOR_EXTENDED token returns 200`() {
        val extUser = adminUserRepository.save(
            AdminUserEntity(
                email = "dashboard-ext-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2024"),
                role = Role.DOCTOR_EXTENDED
            )
        )
        try {
            val cookie = authCookie(extUser)
            mockMvc.get("/api/admin/dashboard") {
                cookie(cookie)
            }.andExpect {
                status { isOk() }
            }
        } finally {
            adminUserRepository.delete(extUser)
        }
    }

    @Test
    fun `getStats ageGender always returns all 6 age groups in order`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/dashboard") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.ageGender.length()") { value(6) }
            jsonPath("$.ageGender[0].ageGroup") { value("<30") }
            jsonPath("$.ageGender[1].ageGroup") { value("30-39") }
            jsonPath("$.ageGender[2].ageGroup") { value("40-49") }
            jsonPath("$.ageGender[3].ageGroup") { value("50-59") }
            jsonPath("$.ageGender[4].ageGroup") { value("60-69") }
            jsonPath("$.ageGender[5].ageGroup") { value("70+") }
        }
    }

    @Test
    fun `getStats weightBmi always returns all 4 BMI categories in order`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/dashboard") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.weightBmi.length()") { value(4) }
            jsonPath("$.weightBmi[0].category") { value("Недостаточный") }
            jsonPath("$.weightBmi[1].category") { value("Нормальный") }
            jsonPath("$.weightBmi[2].category") { value("Избыточный") }
            jsonPath("$.weightBmi[3].category") { value("Ожирение") }
        }
    }

    @Test
    fun `getStats alcohol always returns all 3 categories in order`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/dashboard") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.alcohol.length()") { value(3) }
            jsonPath("$.alcohol[0].category") { value("Употребляю") }
            jsonPath("$.alcohol[1].category") { value("Употреблял ранее") }
            jsonPath("$.alcohol[2].category") { value("Никогда не употреблял") }
        }
    }
}
