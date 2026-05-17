package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
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
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "upload-test@cardio.ru",
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
}
