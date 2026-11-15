package com.medical.cardio.service

import com.medical.cardio.repository.AdminUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import com.medical.cardio.dto.DoctorResponse
import com.medical.cardio.dto.CreateDoctorRequest
import com.medical.cardio.entity.Role
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.AdminSection
import org.springframework.transaction.annotation.Transactional

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

    @Transactional(readOnly = true)
    fun getDoctors(): List<DoctorResponse> {
        val doctors = adminUserRepository.findAllByRole(Role.DOCTOR)
        return doctors.map { doctorResponse(it) }
    }

    @Transactional
    fun createDoctor(request: CreateDoctorRequest): DoctorResponse {
        if (adminUserRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Пользователь с таким email уже существует")
        }

        val user = AdminUserEntity(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            role = Role.DOCTOR,
            allowedSections = request.allowedSections.toMutableSet()
        )

        val savedUser = adminUserRepository.save(user)
        return doctorResponse(savedUser)
    }

    @Transactional
    fun updateDoctorSections(userId: Long, allowedSections: Set<AdminSection>): DoctorResponse {
        val user = adminUserRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User with id $userId not found") }
        require(user.role == Role.DOCTOR) { "Изменять разделы можно только у врача" }
        user.allowedSections.clear()
        user.allowedSections.addAll(allowedSections)
        return doctorResponse(user)
    }

    private fun doctorResponse(user: AdminUserEntity): DoctorResponse =
        DoctorResponse(user.id, user.email, user.isBlocked, user.allowedSections.toSet())

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
