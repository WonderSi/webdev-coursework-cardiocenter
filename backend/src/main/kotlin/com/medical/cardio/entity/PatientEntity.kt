package com.medical.cardio.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "patients")
class PatientEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "create_date", nullable = false)
    val createDate: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: AdminUserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", nullable = false)
    val gender: GlossaryValueEntity,

    @Column(nullable = false)
    val age: Int,

    val height: BigDecimal? = null,

    val weight: BigDecimal? = null,

    @Column(name = "hip_measurement")
    val hipMeasurement: BigDecimal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alcohol_id")
    val alcohol: GlossaryValueEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profession_id")
    val profession: GlossaryValueEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    val region: GlossaryValueEntity? = null,

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

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    val diagnoses: List<DiagnosisEntity> = emptyList()
)
