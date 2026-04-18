package com.medical.cardio.dto

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
    @field:Min(1) @field:Max(120)
    val age: Int? = null,

    @field:NotNull
    @field:DecimalMin("50.0") @field:DecimalMax("250.0")
    val height: BigDecimal? = null,

    @field:NotNull
    @field:DecimalMin("10.0") @field:DecimalMax("300.0")
    val weight: BigDecimal? = null,

    @field:DecimalMin("30.0") @field:DecimalMax("200.0")
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
    @field:Min(1950) @field:Max(2026)
    val strokeYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val heartFailure: Int? = null,
    @field:Min(1950) @field:Max(2026)
    val heartFailureYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val cad: Int? = null,
    @field:Min(1950) @field:Max(2026)
    val cadYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val angina: Int? = null,
    @field:Min(1950) @field:Max(2026)
    val anginaYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val myocardialInfarction: Int? = null,
    @field:Min(1950) @field:Max(2026)
    val myocardialInfarctionYear: Int? = null,

    @field:Min(0) @field:Max(1)
    val arterialHypertension: Int? = null,
    @field:Min(1950) @field:Max(2026)
    val arterialHypertensionYear: Int? = null,

    @field:Valid
    val diagnoses: List<DiagnosisRequest> = emptyList()
)

data class DiagnosisRequest(
    @field:Positive
    val glossaryValueId: Long,

    @field:Min(1900)
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
