package com.medical.cardio.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SurveyRequest(
    @field:NotNull @field:Min(1) @field:Max(2) val gender: Short?,
    @field:NotNull val age: Int?,
    val height: BigDecimal? = null,
    val weight: BigDecimal? = null,
    val hipMeasurement: BigDecimal? = null,
    val alcohol: Short? = null,
    val profession: Short? = null,
    val region: Short? = null,
    val glucose: BigDecimal? = null,
    val cholesterol: BigDecimal? = null,
    val nonHdlCholesterol: BigDecimal? = null,
    val vldlCholesterol: BigDecimal? = null,
    val hdlCholesterol: BigDecimal? = null,
    val ldlCholesterol: BigDecimal? = null,
    val apolipoproteinA: BigDecimal? = null,
    val apolipoproteinB: BigDecimal? = null,
    val triglycerides: BigDecimal? = null,
    val stroke: Boolean? = null,
    val strokeYear: Int? = null,
    val heartFailure: Boolean? = null,
    val heartFailureYear: Int? = null,
    val cad: Boolean? = null,
    val cadYear: Int? = null,
    val angina: Boolean? = null,
    val anginaYear: Int? = null,
    val myocardialInfarction: Boolean? = null,
    val myocardialInfarctionYear: Int? = null,
    val arterialHypertension: Boolean? = null,
    val arterialHypertensionYear: Int? = null,
    val diagnoses: List<DiagnosisRequest> = emptyList()
)

data class DiagnosisRequest(
    val glossaryValueId: Long,
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
