package com.medical.cardio.service.upload

object RowValidator {

    data class ValidationResult(val valid: Boolean, val reason: String = "")

    private val GLOSSARY_FIELDS = listOf("alcohol" to "alcohol", "profession" to "profession", "region" to "region")
    private val NUMERIC_FIELDS = listOf(
        "height", "weight", "hip_measurement", "glucose", "cholesterol",
        "non_hdl_cholesterol", "vldl_cholesterol", "hdl_cholesterol",
        "ldl_cholesterol", "apolipoprotein_a", "apolipoprotein_b", "triglycerides"
    )
    private val DIAGNOSIS_YEAR_FIELDS = listOf(
        "stroke_year", "heart_failure_year", "cad_year",
        "angina_year", "mi_year", "ah_year"
    )

    fun validate(row: Map<String, String>, resolver: GlossaryResolver): ValidationResult {
        val genderStr = row["gender"]?.trim()
        if (genderStr.isNullOrEmpty()) return ValidationResult(false, "Обязательное поле 'gender' отсутствует")
        val genderCode = genderStr.toIntOrNull()
            ?: return ValidationResult(false, "Поле 'gender' должно быть числом")
        if (!resolver.isValidCode("gender", genderCode))
            return ValidationResult(false, "Неверный код пола: $genderCode")

        val ageStr = row["age"]?.trim()
        if (ageStr.isNullOrEmpty()) return ValidationResult(false, "Обязательное поле 'age' отсутствует")
        val age = ageStr.toIntOrNull()
            ?: return ValidationResult(false, "Поле 'age' должно быть числом")
        if (age <= 0 || age > 150) return ValidationResult(false, "Недопустимое значение 'age': $age")

        for ((col, glossary) in GLOSSARY_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty()) {
                val code = str.toIntOrNull()
                    ?: return ValidationResult(false, "Поле '$col' должно быть числом")
                if (!resolver.isValidCode(glossary, code))
                    return ValidationResult(false, "Неверный код поля '$col': $code")
            }
        }

        for (col in NUMERIC_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty() && str.toBigDecimalOrNull() == null)
                return ValidationResult(false, "Поле '$col' должно быть числом: '$str'")
        }

        for (col in DIAGNOSIS_YEAR_FIELDS) {
            val str = row[col]?.trim()
            if (!str.isNullOrEmpty()) {
                val year = str.toIntOrNull()
                    ?: return ValidationResult(false, "Год '$col' должен быть числом")
                if (year < 1900 || year > 2100)
                    return ValidationResult(false, "Недопустимый год '$col': $year")
            }
        }

        return ValidationResult(true)
    }
}
