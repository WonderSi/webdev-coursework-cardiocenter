package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.post

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
                role = Role.DOCTOR_EXTENDED
            )
        )
    }

    @AfterEach
    fun tearDown() {
        adminUserRepository.delete(doctorUser)
        adminUserRepository.delete(extendedUser)
    }

    @Test
    fun `resetPassword with DOCTOR_EXTENDED returns 200`() {
        val cookie = authCookie(extendedUser)
        mockMvc.post("/api/admin/extended/users/${doctorUser.id}/reset-password") {
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
        mockMvc.post("/api/admin/extended/users/${extendedUser.id}/reset-password") {
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
        mockMvc.post("/api/admin/extended/users/${doctorUser.id}/reset-password") {
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
        mockMvc.post("/api/admin/extended/users/${Long.MAX_VALUE}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
            cookie(cookie)
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `resetPassword without token returns 401`() {
        mockMvc.post("/api/admin/extended/users/${doctorUser.id}/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"newPassword":"NewValidPassword@2000"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
