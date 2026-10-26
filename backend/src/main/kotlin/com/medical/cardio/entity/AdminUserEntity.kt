package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "admin_users")
class AdminUserEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 255)
    val email: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @Column(name = "is_blocked", nullable = false)
    var isBlocked: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role = Role.DOCTOR
)
