package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "diagnoses")
class Diagnosis(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    val patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    val diagnosis: GlossaryValue,

    @Column(name = "year_of_diagnosis")
    val yearOfDiagnosis: Short? = null
)
