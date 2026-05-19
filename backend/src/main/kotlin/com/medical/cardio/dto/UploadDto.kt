package com.medical.cardio.dto

import java.math.BigDecimal

data class UploadReport(
    val totalRows: Int,
    val savedRows: Int,
    val skippedRows: Int,
    val errors: List<RowError>
)

data class RowError(
    val row: Int,
    val reason: String
)

data class PatientManualCreateRequest(
    val gender: Int,
    val age: Int,
    val height: BigDecimal? = null,
    val weight: BigDecimal? = null,
    val hipMeasurement: BigDecimal? = null,
    val alcohol: Int? = null,
    val profession: Int? = null,
    val region: Int? = null,
    val glucose: BigDecimal? = null,
    val cholesterol: BigDecimal? = null,
    val nonHdlCholesterol: BigDecimal? = null,
    val vldlCholesterol: BigDecimal? = null,
    val hdlCholesterol: BigDecimal? = null,
    val ldlCholesterol: BigDecimal? = null,
    val apolipoproteinA: BigDecimal? = null,
    val apolipoproteinB: BigDecimal? = null,
    val triglycerides: BigDecimal? = null,
    val stroke: Int? = null,
    val strokeYear: Int? = null,
    val heartFailure: Int? = null,
    val heartFailureYear: Int? = null,
    val cad: Int? = null,
    val cadYear: Int? = null,
    val angina: Int? = null,
    val anginaYear: Int? = null,
    val myocardialInfarction: Int? = null,
    val myocardialInfarctionYear: Int? = null,
    val arterialHypertension: Int? = null,
    val arterialHypertensionYear: Int? = null,
)
