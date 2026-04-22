package com.medical.cardio.service

import com.medical.cardio.repository.AdminUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val adminUserRepository: AdminUserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun resetPassword(userId: Long, newPassword: String) {
        val user = adminUserRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User with id $userId not found") }

        user.passwordHash = passwordEncoder.encode(newPassword)
        adminUserRepository.save(user)
    }
}
