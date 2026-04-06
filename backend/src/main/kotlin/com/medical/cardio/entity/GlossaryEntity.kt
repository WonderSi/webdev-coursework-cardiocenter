package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "glossaries")
class GlossaryEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 100)
    val name: String,

    @OneToMany(mappedBy = "glossary", fetch = FetchType.LAZY)
    val values: List<GlossaryValueEntity> = emptyList()
)
