package com.medical.cardio.service.upload

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.PatientEntity

object PatientMapper {
    // Called only after RowValidator has accepted this observation.
    fun map(row: Map<String, String>, resolver: GlossaryResolver, creator: AdminUserEntity): PatientEntity =
        PatientEntity(
            creator = creator,
            gender = requireNotNull(resolver.resolve("gender", row.getValue("gender").trim().toInt())),
            age = row.getValue("age").trim().toInt(),
            height = row.getValue("height").trim().toBigDecimal(),
            weight = row.getValue("weight").trim().toBigDecimal(),
            hipMeasurement = row["hip_measurement"]?.trim()?.toBigDecimalOrNull(),
            alcohol = requireNotNull(resolver.resolve("alcohol", row.getValue("alcohol").trim().toInt())),
            profession = requireNotNull(resolver.resolve(PatientImportSchema.PROFESSION_GLOSSARY, row.getValue("profession").trim().toInt())),
            region = requireNotNull(resolver.resolve("region", row.getValue("region").trim().toInt())),
            glucose = row["glucose"]?.trim()?.toBigDecimalOrNull(),
            cholesterol = row["cholesterol"]?.trim()?.toBigDecimalOrNull(),
            hdlCholesterol = row["hdl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            ldlCholesterol = row["ldl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            triglycerides = row["triglycerides"]?.trim()?.toBigDecimalOrNull(),
            modelKind = row.getValue("model_kind").trim(),
            modelYear = row["year"]?.trim()?.toIntOrNull(),
            chronicLungDisease = row.getValue("chronic_lung_disease").trim().toInt()
        )
}
