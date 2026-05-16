package com.medical.cardio.config

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BootstrapAdminConfig(
    private val adminUserRepository: AdminUserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${BOOTSTRAP_ADMIN_EMAIL:admin@niikpssz.ru}") private val email: String,
    @Value("\${BOOTSTRAP_ADMIN_PASSWORD:}") private val password: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun createInitialAdmin() {
        if (adminUserRepository.existsByRole(Role.DOCTOR_EXTENDED)) return

        check(password.isNotBlank()) {
            "BOOTSTRAP_ADMIN_PASSWORD не задан — приложение не может создать первого администратора"
        }

        adminUserRepository.save(
            AdminUserEntity(
                email = email,
                passwordHash = passwordEncoder.encode(password),
                role = Role.DOCTOR_EXTENDED,
            )
        )
        log.info("Bootstrap: создан первый DOCTOR_EXTENDED — $email")
    }
}
