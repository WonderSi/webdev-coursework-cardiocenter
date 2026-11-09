package com.medical.cardio.service

import com.medical.cardio.dto.SurveyRequest
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MlSurveyValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator
    private val valid = SurveyRequest(
        modelKind = "region", chronicLungDisease = 0,
        genderId = 1, age = 45, height = BigDecimal("175"), weight = BigDecimal("80"),
        alcoholId = 1, professionId = 13, regionId = 6
    )

    @Test
    fun `both model kinds enforce their ecology period contract`() {
        assertTrue(validator.validate(valid).isEmpty())
        for (year in listOf(2013, 2017, 2019, 2021)) {
            assertTrue(validator.validate(valid.copy(modelKind = "city", year = year)).isEmpty())
        }
        for (request in listOf(
            valid.copy(modelKind = "city"), valid.copy(modelKind = "city", year = 2026),
            valid.copy(year = 2021), valid.copy(modelKind = "general"), valid.copy(modelKind = null)
        )) assertFalse(validator.validate(request).isEmpty(), request.toString())
    }

    @Test
    fun `rejects invalid ML categories and missing lung disease status locally`() {
        for (request in listOf(
            valid.copy(genderId = 3), valid.copy(alcoholId = 4), valid.copy(professionId = 14),
            valid.copy(regionId = 7), valid.copy(chronicLungDisease = null),
            valid.copy(chronicLungDisease = 2), valid.copy(glucose = BigDecimal("-1"))
        )) assertFalse(validator.validate(request).isEmpty())
    }
}
