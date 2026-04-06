package com.medical.cardio.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "patients")
class Patient(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "create_date", nullable = false)
    val createDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: AdminUser,

    @Column(nullable = false)
    val gender: Short,

    @Column(nullable = false)
    val age: Int,

    val height: BigDecimal? = null,

    val weight: BigDecimal? = null,

    @Column(name = "hip_measurement")
    val hipMeasurement: BigDecimal? = null,

    val alcohol: Short? = null,

    val profession: Short? = null,

    val region: Short? = null,

    val glucose: BigDecimal? = null,

    val cholesterol: BigDecimal? = null,

    @Column(name = "non_hdl_cholesterol")
    val nonHdlCholesterol: BigDecimal? = null,

    @Column(name = "vldl_cholesterol")
    val vldlCholesterol: BigDecimal? = null,

    @Column(name = "hdl_cholesterol")
    val hdlCholesterol: BigDecimal? = null,

    @Column(name = "ldl_cholesterol")
    val ldlCholesterol: BigDecimal? = null,

    @Column(name = "apolipoprotein_a")
    val apolipoproteinA: BigDecimal? = null,

    @Column(name = "apolipoprotein_b")
    val apolipoproteinB: BigDecimal? = null,

    val triglycerides: BigDecimal? = null,

    val stroke: Boolean? = null,

    @Column(name = "stroke_year")
    val strokeYear: Int? = null,

    @Column(name = "heart_failure")
    val heartFailure: Boolean? = null,

    @Column(name = "heart_failure_year")
    val heartFailureYear: Int? = null,

    @Column(name = "cad_chd_ihd")
    val cadChdIhd: Boolean? = null,

    @Column(name = "cad_chd_ihd_year")
    val cadChdIhdYear: Int? = null,

    val angina: Boolean? = null,

    @Column(name = "angina_year")
    val anginaYear: Int? = null,

    @Column(name = "myocardial_infarction")
    val myocardialInfarction: Boolean? = null,

    @Column(name = "myocardial_infarction_year")
    val myocardialInfarctionYear: Int? = null,

    @Column(name = "arterial_hypertension")
    val arterialHypertension: Boolean? = null,

    @Column(name = "arterial_hypertension_year")
    val arterialHypertensionYear: Int? = null,

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    val diagnoses: List<Diagnosis> = emptyList()
)
