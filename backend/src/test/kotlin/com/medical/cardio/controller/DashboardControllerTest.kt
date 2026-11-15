package com.medical.cardio.controller

import com.medical.cardio.entity.AdminSection
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.get

class DashboardControllerTest : ControllerTestBase() {
    @Autowired private lateinit var users: AdminUserRepository
    private lateinit var user: AdminUserEntity

    @BeforeEach
    fun createUser() {
        user = users.save(AdminUserEntity(email = "dashboard-http@test.invalid", passwordHash = "unused-test-hash",
            allowedSections = mutableSetOf(AdminSection.DASHBOARDS)))
    }

    @AfterEach
    fun deleteUser() { users.deleteById(user.id) }

    @Test
    fun `doctor gets new observation contract with default city`() {
        mockMvc.get("/api/admin/dashboard") { cookie(authCookie(user)) }.andExpect {
            status { isOk() }
            jsonPath("$.modelKind") { value("city") }
            jsonPath("$.totalRecords") { exists() }
            jsonPath("$.genderDistribution") { isArray() }
            jsonPath("$.bmi.length()") { value(5) }
            jsonPath("$.ageGender.length()") { value(6) }
            jsonPath("$.lungDisease.length()") { value(3) }
            jsonPath("$.labCoverage.length()") { value(5) }
            jsonPath("$.genderDiagnoses") { doesNotExist() }
            jsonPath("$.healthyCount") { doesNotExist() }
        }
    }

    @Test
    fun `region can be selected and unknown model is rejected`() {
        mockMvc.get("/api/admin/dashboard?modelKind=region") { cookie(authCookie(user)) }.andExpect {
            status { isOk() }
            jsonPath("$.modelKind") { value("region") }
        }
        mockMvc.get("/api/admin/dashboard?modelKind=unknown") { cookie(authCookie(user)) }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `anonymous request requires authentication`() {
        mockMvc.get("/api/admin/dashboard").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `super admin does not need explicit dashboard grant`() {
        val admin = users.save(AdminUserEntity(email = "dashboard-super@test.invalid", passwordHash = "unused-test-hash", role = Role.SUPER_ADMIN))
        try {
            mockMvc.get("/api/admin/dashboard?modelKind=region") { cookie(authCookie(admin)) }
                .andExpect { status { isOk() } }
        } finally { users.deleteById(admin.id) }
    }
}
