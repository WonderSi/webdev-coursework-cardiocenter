package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put

class AdminControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var doctorUser: AdminUserEntity
    private lateinit var extendedUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        doctorUser = adminUserRepository.save(
            AdminUserEntity(
                email = "admin-doctor@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
        extendedUser = adminUserRepository.save(
            AdminUserEntity(
                email = "admin-extended@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.SUPER_ADMIN
            )
        )
    }

    @AfterEach
    fun tearDown() {
        adminUserRepository.delete(doctorUser)
        adminUserRepository.delete(extendedUser)
        adminUserRepository.findByEmail("doctor@example.com")?.let { adminUserRepository.delete(it) }
    }

    @Test
    fun `resetPassword with SUPER_ADMIN returns 200`() {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/users/${doctorUser.id}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `resetPassword with DOCTOR role returns 403`() {
        val cookie = authCookie(doctorUser)
        mockMvc.post("/api/admin/super-admin/users/${extendedUser.id}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `resetPassword with password too short returns 400`() {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/users/${doctorUser.id}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"short"}"""
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `resetPassword with non-existent user returns 404`() {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/users/${Long.MAX_VALUE}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `resetPassword without token returns 401`() {
        mockMvc.post("/api/admin/super-admin/users/${doctorUser.id}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getDoctors with SUPER_ADMIN returns 200`() {
        val cookie = authCookie(extendedUser)
        mockMvc.get("/api/admin/super-admin/doctors") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            content {
                json(
                    """[{"id":${doctorUser.id},"email":"${doctorUser.email}","isBlocked":${doctorUser.isBlocked}}]""",
                    JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `getDoctors with DOCTOR role returns 403`() {
        val cookie = authCookie(doctorUser)
        mockMvc.get("/api/admin/super-admin/doctors") {
            cookie(cookie)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `getDoctors without token returns 401`() {
        mockMvc.get("/api/admin/super-admin/doctors")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `createDoctor with DOCTOR role returns 403` () {
        val cookie = authCookie(doctorUser)
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"doctor@example.com","password":"ValidPass@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `createDoctor without token returns 401` () {
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"doctor@example.com","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `createDoctor with SUPER_ADMIN returns 201` () {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"doctor@example.com","password":"ValidPass@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isCreated() }
        }

        val savedDoctor = adminUserRepository.findByEmail("doctor@example.com") 
            ?: error("Созданный врач не найден в БД")
        
        assertEquals(Role.DOCTOR, savedDoctor.role)
        assertTrue(passwordEncoder.matches("ValidPass@2000", savedDoctor.passwordHash))
    }

    @Test
    fun `createDoctor with existing email returns 400` () {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"${doctorUser.email}","password":"ValidPass@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `createDoctor with invalid email returns 400` () {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"invalid-email","password":"ValidPass@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `createDoctor with short password returns 400` () {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/super-admin/doctors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"doctor@example.com","password":"short"}"""
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `updateDoctorBlocked with SUPER_ADMIN blocks doctor returns 204` () {
        val cookie = authCookie(extendedUser)
        mockMvc.put("/api/admin/super-admin/doctors/${doctorUser.id}/blocked") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"isBlocked":true}"""
            cookie(cookie)
        }.andExpect {
            status { isNoContent() }
        }

        val updatedDoctor = adminUserRepository.findById(doctorUser.id).orElseThrow()
        assertTrue(updatedDoctor.isBlocked)
    }

    @Test
    fun `updateDoctorBlocked with SUPER_ADMIN unblocks doctor returns 204` () {
        doctorUser.isBlocked = true
        adminUserRepository.save(doctorUser)

        val cookie = authCookie(extendedUser)
        mockMvc.put("/api/admin/super-admin/doctors/${doctorUser.id}/blocked") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"isBlocked":false}"""
            cookie(cookie)
        }.andExpect {
            status { isNoContent() }
        }

        val updatedDoctor = adminUserRepository.findById(doctorUser.id).orElseThrow()
        assertTrue(!updatedDoctor.isBlocked)
    }

    @Test
    fun `updateDoctorBlocked with DOCTOR role returns 403`() {
        val cookie = authCookie(doctorUser)
        mockMvc.put("/api/admin/super-admin/doctors/${doctorUser.id}/blocked") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"isBlocked":true}"""
            cookie(cookie)
        }.andExpect {
            status { isForbidden() }
        }

        val updatedDoctor = adminUserRepository.findById(doctorUser.id).orElseThrow()
        assertTrue(!updatedDoctor.isBlocked)
    }

    @Test
    fun `updateDoctorBlocked with non-existent doctor returns 404`() {
        val cookie = authCookie(extendedUser)
        mockMvc.put("/api/admin/super-admin/doctors/${Long.MAX_VALUE}/blocked") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"isBlocked":true}"""
            cookie(cookie)
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `updateDoctorBlocked with non-doctor user returns 400`() {
        val cookie = authCookie(extendedUser)
        mockMvc.put("/api/admin/super-admin/doctors/${extendedUser.id}/blocked") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"isBlocked":true}"""
            cookie(cookie)
        }.andExpect {
            status { isBadRequest() }
        }
    }
}