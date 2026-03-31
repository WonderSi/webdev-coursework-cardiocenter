package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "admin_users")
class AdminUser(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String
)
