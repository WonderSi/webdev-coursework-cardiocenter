package com.medical.cardio.service.upload

import com.medical.cardio.validation.MedicalConstraints
import java.math.BigDecimal

object RowValidator {
    data class ValidationResult(val valid: Boolean, val reason: String = "")

    private val glossaryFields = mapOf(
        "gender" to ("gender" to 1..2),
        "alcohol" to ("alcohol" to 1..3),
        "profession" to (PatientImportSchema.PROFESSION_GLOSSARY to 1..13),
        "region" to ("region" to 1..6)
    )
    private val rangedFields = mapOf(
        "height" to (MedicalConstraints.HEIGHT_MIN.toBigDecimal() to MedicalConstraints.HEIGHT_MAX.toBigDecimal()),
        "weight" to (MedicalConstraints.WEIGHT_MIN.toBigDecimal() to MedicalConstraints.WEIGHT_MAX.toBigDecimal()),
        "hip_measurement" to (MedicalConstraints.HIP_MIN.toBigDecimal() to MedicalConstraints.HIP_MAX.toBigDecimal())
    )
    private val labFields = listOf("glucose", "cholesterol", "hdl_cholesterol", "ldl_cholesterol", "triglycerides")

    fun validate(row: Map<String, String>, resolver: GlossaryResolver): ValidationResult {
        val unsupported = row.keys.firstOrNull { it !in PatientImportSchema.headers }
        if (unsupported != null)
            return ValidationResult(false, "Неизвестный столбец '$unsupported'. Используйте актуальный шаблон импорта обследований")

        val kind = row["model_kind"]?.trim()
        if (kind !in setOf("city", "region"))
            return ValidationResult(false, "Поле 'model_kind' обязательно: city или region")
        val year = row["year"]?.trim().orEmpty()
        if (kind == "city" && year !in setOf("2013", "2017", "2019", "2021"))
            return ValidationResult(false, "Для city поле 'year' должно быть 2013, 2017, 2019 или 2021")
        if (kind == "region" && year.isNotEmpty())
            return ValidationResult(false, "Для region поле 'year' должно быть пустым")
        if (row["chronic_lung_disease"]?.trim() !in setOf("0", "1"))
            return ValidationResult(false, "Поле 'chronic_lung_disease' обязательно: явный ответ 0 или 1")

        for ((field, specification) in glossaryFields) {
            val (glossary, range) = specification
            val value = row[field]?.trim()?.toIntOrNull()
                ?: return ValidationResult(false, "Обязательное поле '$field' должно быть целым числом")
            if (value !in range || !resolver.isValidCode(glossary, value))
                return ValidationResult(false, "Неверный код поля '$field': $value")
        }

        val age = row["age"]?.trim()?.toIntOrNull()
            ?: return ValidationResult(false, "Обязательное поле 'age' должно быть целым числом")
        if (age !in MedicalConstraints.AGE_MIN..MedicalConstraints.AGE_MAX)
            return ValidationResult(false, "Недопустимое значение 'age': $age (допустимо ${MedicalConstraints.AGE_MIN}–${MedicalConstraints.AGE_MAX})")

        for ((field, range) in rangedFields) {
            val valueText = row[field]?.trim().orEmpty()
            if (valueText.isEmpty() && field == "hip_measurement") continue
            val value = valueText.toBigDecimalOrNull()
                ?: return ValidationResult(false, "Поле '$field' должно быть числом")
            if (value < range.first || value > range.second)
                return ValidationResult(false, "Недопустимое значение '$field': $value (допустимо ${range.first}–${range.second})")
        }
        for (field in labFields) {
            val valueText = row[field]?.trim().orEmpty()
            if (valueText.isEmpty()) continue
            val value = valueText.toBigDecimalOrNull()
                ?: return ValidationResult(false, "Поле '$field' должно быть числом")
            if (value < BigDecimal.ZERO || value > BigDecimal("9999.99"))
                return ValidationResult(false, "Поле '$field' должно быть неотрицательным и помещаться в NUMERIC(6,2)")
        }
        return ValidationResult(true)
    }
}
