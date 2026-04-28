package com.medical.cardio.repository

import com.medical.cardio.entity.AdminUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<AdminUserEntity, Long> {
    // Вызывается при каждом логине и валидации JWT
    fun findByEmail(email: String): AdminUserEntity?

    fun existsByEmail(email: String): Boolean
}
