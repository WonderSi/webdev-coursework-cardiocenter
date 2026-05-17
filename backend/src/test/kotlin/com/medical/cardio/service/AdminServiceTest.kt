package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder

class AdminServiceTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var adminService: AdminService

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private var savedUser: AdminUserEntity? = null

    @AfterEach
    fun tearDown() {
        savedUser?.let { adminUserRepository.delete(it) }
        savedUser = null
    }

    @Test
    fun `resetPassword updates password hash`() {
        val user = adminUserRepository.save(
            AdminUserEntity(
                email = "service-reset-test@cardio.ru",
                passwordHash = passwordEncoder.encode("OldPassword@2024"),
                role = Role.DOCTOR
            )
        )
        savedUser = user

        val newPassword = "NewValidPass@2024"
        adminService.resetPassword(user.id, newPassword)

        val updated = adminUserRepository.findById(user.id).orElseThrow()
        assertTrue(passwordEncoder.matches(newPassword, updated.passwordHash))
    }

    @Test
    fun `resetPassword with non-existent user throws NoSuchElementException`() {
        assertThrows<NoSuchElementException> {
            adminService.resetPassword(Long.MAX_VALUE, "ValidNewPass@2024")
        }
    }
}
