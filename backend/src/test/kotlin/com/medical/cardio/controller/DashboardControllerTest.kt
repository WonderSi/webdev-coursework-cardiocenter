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
                passwordHash = passwordEncoder.encode("ValidPass@2024"),
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
}
