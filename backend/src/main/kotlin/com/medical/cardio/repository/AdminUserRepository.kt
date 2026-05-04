package com.medical.cardio.repository

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import org.springframework.data.jpa.repository.JpaRepository

interface AdminUserRepository : JpaRepository<AdminUserEntity, Long> {
    fun findByEmail(email: String): AdminUserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByRole(role: Role): Boolean
}
