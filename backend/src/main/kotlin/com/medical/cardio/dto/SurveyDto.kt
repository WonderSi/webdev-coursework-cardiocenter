package com.medical.cardio.dto

import com.medical.cardio.validation.MedicalConstraints
import com.medical.cardio.validation.NotFutureYear
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class SurveyRequest(
    @field:NotNull
    @field:Positive
    val genderId: Long? = null,

    @field:NotNull
    @field:Min(MedicalConstraints.AGE_MIN.toLong()) @field:Max(MedicalConstraints.AGE_MAX.toLong())
    val age: Int? = null,

    @field:NotNull
    @field:DecimalMin(MedicalConstraints.HEIGHT_MIN) @field:DecimalMax(MedicalConstraints.HEIGHT_MAX)
    val height: BigDecimal? = null,

    @field:NotNull
    @field:DecimalMin(MedicalConstraints.WEIGHT_MIN) @field:DecimalMax(MedicalConstraints.WEIGHT_MAX)
    val weight: BigDecimal? = null,

    @field:DecimalMin(MedicalConstraints.HIP_MIN) @field:DecimalMax(MedicalConstraints.HIP_MAX)
    val hipMeasurement: BigDecimal? = null,

    @field:NotNull
    @field:Positive
    val alcoholId: Long? = null,

    @field:NotNull
    @field:Positive
    val professionId: Long? = null,

    @field:NotNull
    @field:Positive
    val regionId: Long? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("50.0")
    val glucose: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("30.0")
    val cholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("30.0")
    val nonHdlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    val vldlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    val hdlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("20.0")
    val ldlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    val apolipoproteinA: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    val apolipoproteinB: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("20.0")
    val triglycerides: BigDecimal? = null,

    @field:Min(0) @field:Max(1)
    val stroke: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val strokeYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val heartFailure: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val heartFailureYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val cad: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val cadYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val angina: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val anginaYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val myocardialInfarction: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val myocardialInfarctionYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val arterialHypertension: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val arterialHypertensionYear: Int? = null,

    @field:Valid
    val diagnoses: List<DiagnosisRequest> = emptyList()
)

data class DiagnosisRequest(
    @field:Positive
    val glossaryValueId: Long,

    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    val yearOfDiagnosis: Int?
)

data class SurveyResponse(
    val prediction: PredictionResult? = null
)

data class PredictionResult(
    val diseaseType: String?,
    val probability: String? = null,
    val message: String
)

data class GlossaryResponse(
    val id: Long,
    val name: String,
    val values: List<GlossaryValueResponse>
)

data class GlossaryValueResponse(
    val id: Long,
    val code: Int,
    val value: String
)
