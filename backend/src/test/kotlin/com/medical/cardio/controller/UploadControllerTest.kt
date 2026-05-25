package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.PatientRepository
import com.medical.cardio.support.ControllerTestBase
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart

class UploadControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var patientRepository: PatientRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "upload-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
    }

    @AfterEach
    fun tearDown() {
        // Delete patients first (diagnoses cascade via ON DELETE CASCADE), then the admin user
        patientRepository.deleteAll()
        adminUserRepository.delete(testUser)
    }

    @Test
    fun `downloadTemplate with auth returns 200 and xlsx content-disposition`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/admin/upload/template") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            header { string("Content-Disposition", containsString("patients_template.xlsx")) }
        }
    }

    @Test
    fun `downloadTemplate without auth returns 401`() {
        mockMvc.get("/api/admin/upload/template").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `upload with wrong file extension returns 400`() {
        val cookie = authCookie(testUser)
        val file = MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "fake-pdf-content".toByteArray()
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `upload without auth returns 401`() {
        val file = MockMultipartFile(
            "file",
            "test.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "fake-pdf-content".toByteArray()
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `upload valid CSV returns 200 with upload report`() {
        val cookie = authCookie(testUser)
        val csvContent = "gender,age\n1,45"
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(1) }
            jsonPath("$.skippedRows") { value(0) }
        }
    }

    @Test
    fun `upload empty file returns 400`() {
        val cookie = authCookie(testUser)
        val file = MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            ByteArray(0)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `upload CSV with invalid gender code skips the row`() {
        val cookie = authCookie(testUser)
        val csvContent = "gender,age\n99,45"
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(0) }
            jsonPath("$.skippedRows") { value(1) }
        }
    }

    @Test
    fun `upload CSV with age below minimum skips the row`() {
        val cookie = authCookie(testUser)
        val csvContent = "gender,age\n1,10"
        val file = MockMultipartFile(
            "file",
            "patients.csv",
            "text/csv",
            csvContent.toByteArray(Charsets.UTF_8)
        )
        mockMvc.multipart("/api/admin/upload") {
            file(file)
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalRows") { value(1) }
            jsonPath("$.savedRows") { value(0) }
            jsonPath("$.skippedRows") { value(1) }
        }
    }
}
