package com.medical.cardio.entity

import jakarta.persistence.*

@Entity
@Table(name = "diagnoses")
class DiagnosisEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    val patient: PatientEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    val diagnosis: GlossaryValueEntity,

    @Column(name = "year_of_diagnosis")
    val yearOfDiagnosis: Short? = null
)
