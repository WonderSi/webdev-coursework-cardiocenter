package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "glossary_values")
class GlossaryValue(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "glossary_id", nullable = false)
    val glossary: Glossary,

    @Column(nullable = false)
    val code: Int,

    @Column(nullable = false, length = 255)
    val value: String
)
