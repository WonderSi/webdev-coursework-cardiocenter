package com.medical.cardio.service.upload

import com.medical.cardio.validation.MedicalConstraints
import java.time.Year

object RowValidator {

    data class ValidationResult(val valid: Boolean, val reason: String = "")

    private val GLOSSARY_FIELDS = listOf("alcohol", "profession", "region")

    private val RANGED_FIELDS = mapOf(
        "height"          to (MedicalConstraints.HEIGHT_MIN.toBigDecimal() to MedicalConstraints.HEIGHT_MAX.toBigDecimal()),
        "weight"          to (MedicalConstraints.WEIGHT_MIN.toBigDecimal() to MedicalConstraints.WEIGHT_MAX.toBigDecimal()),
        "hip_measurement" to (MedicalConstraints.HIP_MIN.toBigDecimal()    to MedicalConstraints.HIP_MAX.toBigDecimal()),
    )

    private val LAB_FIELDS = listOf(
        "glucose", "cholesterol", "non_hdl_cholesterol", "vldl_cholesterol",
        "hdl_cholesterol", "ldl_cholesterol", "apolipoprotein_a", "apolipoprotein_b", "triglycerides"
    )

    private val DIAGNOSIS_FLAG_FIELDS = listOf(
        "stroke", "heart_failure", "cad", "angina", "myocardial_infarction", "arterial_hypertension"
    )

    private val DIAGNOSIS_YEAR_FIELDS = listOf(
        "stroke_year", "heart_failure_year", "cad_year",
        "angina_year", "mi_year", "ah_year"
    )

    fun validate(row: Map<String, String>, resolver: GlossaryResolver): ValidationResult {

        // Gender (required, glossary code)
        val genderStr = row["gender"]?.trim()
        if (genderStr.isNullOrEmpty()) return ValidationResult(false, "Обязательное поле 'gender' отсутствует")
        val genderCode = genderStr.toIntOrNull()
            ?: return ValidationResult(false, "Поле 'gender' должно быть числом")
        if (!resolver.isValidCode("gender", genderCode))
            return ValidationResult(false, "Неверный код пола: $genderCode")

        // Age (required, range)
        val ageStr = row["age"]?.trim()
        if (ageStr.isNullOrEmpty()) return ValidationResult(false, "Обязательное поле 'age' отсутствует")
        val age = ageStr.toIntOrNull()
            ?: return ValidationResult(false, "Поле 'age' должно быть числом")
        if (age !in MedicalConstraints.AGE_MIN..MedicalConstraints.AGE_MAX)
            return ValidationResult(false, "Недопустимое значение 'age': $age (допустимо ${MedicalConstraints.AGE_MIN}–${MedicalConstraints.AGE_MAX})")

        // Glossary fields (optional, valid code if present)
        for (col in GLOSSARY_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty()) {
                val code = str.toIntOrNull()
                    ?: return ValidationResult(false, "Поле '$col' должно быть числом")
                if (!resolver.isValidCode(col, code))
                    return ValidationResult(false, "Неверный код поля '$col': $code")
            }
        }

        // Numeric fields with range (optional)
        for ((col, range) in RANGED_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty()) {
                val value = str.toBigDecimalOrNull()
                    ?: return ValidationResult(false, "Поле '$col' должно быть числом: '$str'")
                if (value < range.first || value > range.second)
                    return ValidationResult(false, "Недопустимое значение '$col': $value (допустимо ${range.first}–${range.second})")
            }
        }

        // Lab fields (optional, type check only)
        for (col in LAB_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty() && str.toBigDecimalOrNull() == null)
                return ValidationResult(false, "Поле '$col' должно быть числом: '$str'")
        }

        // Diagnosis flags (optional, 0 or 1)
        for (col in DIAGNOSIS_FLAG_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty() && str !in setOf("0", "1"))
                return ValidationResult(false, "Поле '$col' должно быть 0 или 1: '$str'")
        }

        // Diagnosis years (optional, 1900–currentYear)
        val currentYear = Year.now().value
        for (col in DIAGNOSIS_YEAR_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty()) {
                val year = str.toIntOrNull()
                    ?: return ValidationResult(false, "Год '$col' должен быть числом")
                if (year !in MedicalConstraints.YEAR_MIN..currentYear)
                    return ValidationResult(false, "Недопустимый год '$col': $year (допустимо ${MedicalConstraints.YEAR_MIN}–$currentYear)")
            }
        }

        return ValidationResult(true)
    }
}
