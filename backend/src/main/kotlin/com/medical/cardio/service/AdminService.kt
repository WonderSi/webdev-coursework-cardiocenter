package com.medical.cardio.service

import com.medical.cardio.repository.AdminUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.medical.cardio.dto.DoctorResponse
import com.medical.cardio.dto.CreateDoctorRequest
import com.medical.cardio.entity.Role
import com.medical.cardio.entity.AdminUserEntity

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

    fun getDoctors(): List<DoctorResponse> {
        val doctors = adminUserRepository.findAllByRole(Role.DOCTOR)
        return doctors.map { DoctorResponse(it.id, it.email, it.isBlocked) }
    }

    fun createDoctor(request: CreateDoctorRequest): DoctorResponse {
        if (adminUserRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Пользователь с таким email уже существует")
        }

        val user = AdminUserEntity(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            role = Role.DOCTOR
        )

        val savedUser = adminUserRepository.save(user)
        return DoctorResponse(savedUser.id, savedUser.email, savedUser.isBlocked)
    }

    fun updateDoctorBlocked(userId: Long, isBlocked: Boolean) {
        val user = adminUserRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User with id $userId not found") }

        if (user.role != Role.DOCTOR) {
            throw IllegalArgumentException("Изменять блокировку можно только у врача")
        }

        user.isBlocked = isBlocked
        adminUserRepository.save(user)
    }
}
