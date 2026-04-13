package com.medical.cardio.dto
import java.math.BigDecimal

data class SurveyRequest(
    val genderId: Long? = null,
    val age: Int? = null,
    val height: BigDecimal? = null,
    val weight: BigDecimal? = null,
    val hipMeasurement: BigDecimal? = null,
    val alcoholId: Long? = null,
    val professionId: Long? = null,
    val regionId: Long? = null,
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
