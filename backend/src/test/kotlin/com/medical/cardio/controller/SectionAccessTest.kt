package com.medical.cardio.controller

import com.medical.cardio.entity.AdminSection
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.service.AdminService
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.multipart

@TestPropertySource(properties = ["spring.jpa.open-in-view=false"])
class SectionAccessTest : ControllerTestBase() {
    @Autowired private lateinit var users: AdminUserRepository
    @Autowired private lateinit var adminService: AdminService
    private lateinit var doctor: AdminUserEntity

    @BeforeEach
    fun createDoctor() {
        doctor = users.saveAndFlush(AdminUserEntity(
            email = "section-access@cardio.test", passwordHash = "unused-test-hash"))
    }

    @AfterEach
    fun deleteDoctor() { users.deleteById(doctor.id) }

    @ParameterizedTest
    @ValueSource(strings = ["/api/admin/dashboard", "/api/admin/upload/template",
        "/api/admin/model", "/api/admin/dataset/schema", "/api/admin/unknown"])
    fun `doctor without section cannot read protected data`(path: String) {
        mockMvc.get(path) { cookie(authCookie(doctor)) }.andExpect {
            status { isForbidden() }
            jsonPath("$.code") { value("FORBIDDEN") }
        }
        mockMvc.get(path).andExpect { status { isUnauthorized() } }
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api/admin/upload", "/api/admin/patients", "/api/admin/retrain"])
    fun `doctor without section cannot write protected data`(path: String) {
        mockMvc.post(path) { cookie(authCookie(doctor)) }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `permissions change on next request with same cookie and sections stay independent`() {
        val cookie = authCookie(doctor)
        adminService.updateDoctorSections(doctor.id, setOf(AdminSection.DASHBOARDS))
        mockMvc.get("/api/admin/dashboard") { cookie(cookie) }.andExpect { status { isOk() } }
        mockMvc.get("/api/admin/upload/template") { cookie(cookie) }.andExpect { status { isForbidden() } }
        mockMvc.get("/api/auth/me") { cookie(cookie) }.andExpect {
            status { isOk() }
            jsonPath("$.role") { value("ROLE_DOCTOR") }
            jsonPath("$.allowedSections[0]") { value("DASHBOARDS") }
        }

        adminService.updateDoctorSections(doctor.id, setOf(AdminSection.UPLOAD_DATA))
        mockMvc.get("/api/admin/dashboard") { cookie(cookie) }.andExpect { status { isForbidden() } }
        mockMvc.get("/api/admin/upload/template") { cookie(cookie) }.andExpect { status { isOk() } }
        mockMvc.get("/api/admin/super-admin/doctors") { cookie(cookie) }.andExpect { status { isForbidden() } }

        adminService.updateDoctorSections(doctor.id, emptySet())
        mockMvc.get("/api/admin/upload/template") { cookie(cookie) }.andExpect { status { isForbidden() } }
        mockMvc.get("/api/auth/me") { cookie(cookie) }.andExpect {
            status { isOk() }
            jsonPath("$.allowedSections") { isEmpty() }
        }
    }

    @Test
    fun `view data section does not grant unrelated APIs`() {
        adminService.updateDoctorSections(doctor.id, setOf(AdminSection.VIEW_DATA))
        mockMvc.get("/api/admin/dashboard") { cookie(authCookie(doctor)) }.andExpect { status { isForbidden() } }
        mockMvc.get("/api/admin/upload/template") { cookie(authCookie(doctor)) }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `retraining permission is independent and revocable with same cookie`() {
        val cookie = authCookie(doctor)
        adminService.updateDoctorSections(doctor.id, setOf(AdminSection.MODEL_TRAINING))
        mockMvc.post("/api/admin/retrain") { cookie(cookie) }.andExpect { status { isForbidden() } }

        adminService.updateDoctorSections(doctor.id, setOf(AdminSection.RETRAIN))

        mockMvc.multipart("/api/admin/retrain") { cookie(cookie) }.andExpect { status { isBadRequest() } }
        mockMvc.get("/api/admin/model") { cookie(cookie) }.andExpect { status { isForbidden() } }

        adminService.updateDoctorSections(doctor.id, emptySet())
        mockMvc.post("/api/admin/retrain") { cookie(cookie) }.andExpect { status { isForbidden() } }
    }
}
