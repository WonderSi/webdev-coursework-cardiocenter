package com.medical.cardio.service.upload

import com.medical.cardio.dto.PatientManualCreateRequest

object PatientImportSchema {
    const val PROFESSION_GLOSSARY = "profession"

    val headers: List<String> = listOf(
        "model_kind", "year", "chronic_lung_disease", "gender", "age", "height", "weight",
        "hip_measurement", "alcohol", "profession", "region", "glucose", "cholesterol",
        "hdl_cholesterol", "ldl_cholesterol", "triglycerides"
    )

    fun toRow(request: PatientManualCreateRequest): Map<String, String> = mapOf(
        "model_kind" to request.modelKind,
        "year" to request.year,
        "chronic_lung_disease" to request.chronicLungDisease,
        "gender" to request.gender,
        "age" to request.age,
        "height" to request.height,
        "weight" to request.weight,
        "hip_measurement" to request.hipMeasurement,
        "alcohol" to request.alcohol,
        "profession" to request.profession,
        "region" to request.region,
        "glucose" to request.glucose,
        "cholesterol" to request.cholesterol,
        "hdl_cholesterol" to request.hdlCholesterol,
        "ldl_cholesterol" to request.ldlCholesterol,
        "triglycerides" to request.triglycerides
    ).mapValues { (_, value) -> value?.toString().orEmpty() }
}
