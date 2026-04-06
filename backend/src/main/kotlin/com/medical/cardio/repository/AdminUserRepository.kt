package com.medical.cardio.repository

import com.medical.cardio.entity.AdminUserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<AdminUserEntity, Long> {
    // Вызывается при каждом логине и валидации JWT
    fun findByUsername(username: String): AdminUserEntity?

    // Проверка уникальности при создании аккаунта
    fun existsByUsername(username: String): Boolean
}
