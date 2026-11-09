package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.medical.cardio.validation.MedicalConstraints
import com.medical.cardio.validation.NotFutureYear
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Pattern
import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal

data class SurveyRequest(
    @field:NotNull
    @field:Pattern(regexp = "city|region")
    @field:Schema(
        description = "Модель прогнозирования: city — отдельное обследование, region — сводные данные. Выбирается отдельно от района проживания.",
        allowableValues = ["city", "region"], example = "city"
    )
    val modelKind: String? = null,

    @field:Schema(
        description = "Период экологических данных: обязателен для city; для region поле отсутствует или равно null.",
        allowableValues = ["2013", "2017", "2019", "2021"], nullable = true, example = "2021"
    )
    val year: Int? = null,

    @field:NotNull
    @field:Min(0) @field:Max(1)
    @field:Schema(
        description = "Хронические заболевания лёгких: 0 — нет, 1 — есть. Требуется явный ответ для обеих моделей.",
        allowableValues = ["0", "1"], example = "0"
    )
    val chronicLungDisease: Int? = null,

    @field:NotNull
    @field:Min(1) @field:Max(2)
    @field:Schema(description = "Код пола, передаваемый в ML", example = "1")
    val genderId: Long? = null,

    @field:NotNull
    @field:Min(MedicalConstraints.AGE_MIN.toLong()) @field:Max(MedicalConstraints.AGE_MAX.toLong())
    @field:Schema(description = "Возраст, полных лет", example = "45")
    val age: Int? = null,

    @field:NotNull
    @field:DecimalMin(MedicalConstraints.HEIGHT_MIN) @field:DecimalMax(MedicalConstraints.HEIGHT_MAX)
    @field:Schema(description = "Рост, см", example = "175")
    val height: BigDecimal? = null,

    @field:NotNull
    @field:DecimalMin(MedicalConstraints.WEIGHT_MIN) @field:DecimalMax(MedicalConstraints.WEIGHT_MAX)
    @field:Schema(description = "Вес, кг", example = "80")
    val weight: BigDecimal? = null,

    @field:DecimalMin(MedicalConstraints.HIP_MIN) @field:DecimalMax(MedicalConstraints.HIP_MAX)
    @field:Schema(description = "Обхват талии/бёдер, см", example = "95")
    val hipMeasurement: BigDecimal? = null,

    @field:NotNull
    @field:Min(1) @field:Max(3)
    @field:Schema(description = "Код употребления алкоголя; перед отправкой ML вычитается 1", example = "3")
    val alcoholId: Long? = null,

    @field:NotNull
    @field:Min(1) @field:Max(13)
    @field:Schema(description = "Код профессии 1–13 по классификации notebook-13-v1 из ML-контракта. Не ID записи БД и не код старого справочника profession (1–14).", example = "2")
    val professionId: Long? = null,

    @field:NotNull
    @field:Min(1) @field:Max(6)
    @field:Schema(description = "Код района, передаваемый в ML", example = "1")
    val regionId: Long? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("50.0")
    @field:DecimalMin("0")
    @field:Schema(description = "Глюкоза, ммоль/л", example = "5.2")
    val glucose: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("30.0")
    @field:DecimalMin("0")
    @field:Schema(description = "Общий холестерин, ммоль/л", example = "4.8")
    val cholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("30.0")
    @field:Schema(description = "Холестерин не-ЛПВП, ммоль/л", example = "3.5")
    val nonHdlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    @field:Schema(description = "Холестерин ЛПОНП, ммоль/л", example = "0.7")
    val vldlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    @field:DecimalMin("0")
    @field:Schema(description = "Холестерин ЛПВП, ммоль/л", example = "1.3")
    val hdlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("20.0")
    @field:DecimalMin("0")
    @field:Schema(description = "Холестерин ЛПНП, ммоль/л", example = "2.8")
    val ldlCholesterol: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    @field:Schema(description = "Аполипопротеин A", example = "1.4")
    val apolipoproteinA: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("10.0")
    @field:Schema(description = "Аполипопротеин B", example = "0.9")
    val apolipoproteinB: BigDecimal? = null,

//    @field:DecimalMin("0.1") @field:DecimalMax("20.0")
    @field:DecimalMin("0")
    @field:Schema(description = "Триглицериды, ммоль/л", example = "1.5")
    val triglycerides: BigDecimal? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Инсульт: 0 — нет, 1 — есть", example = "0")
    val stroke: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Инсульт", example = "2020")
    val strokeYear: Int? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Сердечная недостаточность: 0 — нет, 1 — есть", example = "0")
    val heartFailure: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Сердечная недостаточность", example = "2020")
    val heartFailureYear: Int? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Аритмия / ИБС: 0 — нет, 1 — есть", example = "0")
    val cad: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Аритмия / ИБС", example = "2020")
    val cadYear: Int? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Стенокардия: 0 — нет, 1 — есть", example = "0")
    val angina: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Стенокардия", example = "2020")
    val anginaYear: Int? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Инфаркт миокарда: 0 — нет, 1 — есть", example = "0")
    val myocardialInfarction: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Инфаркт миокарда", example = "2020")
    val myocardialInfarctionYear: Int? = null,

    @field:Min(0) @field:Max(1)
    @field:Schema(description = "Артериальная гипертензия: 0 — нет, 1 — есть", example = "0")
    val arterialHypertension: Int? = null,
    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза: Артериальная гипертензия", example = "2020")
    val arterialHypertensionYear: Int? = null,

    @field:Valid
    @field:Schema(description = "Дополнительные диагнозы; текущий ML-маппер это поле не использует")
    val diagnoses: List<DiagnosisRequest> = emptyList()
) {
    @get:JsonIgnore
    @get:AssertTrue(message = "Для city нужен период 2013, 2017, 2019 или 2021; для region year должен быть null")
    val isModelPeriodValid: Boolean
        get() = when (modelKind) {
            "city" -> year in setOf(2013, 2017, 2019, 2021)
            "region" -> year == null
            else -> true
        }
}

data class DiagnosisRequest(
    @field:Positive
    @field:Schema(description = "ID значения справочника диагнозов", example = "26")
    val glossaryValueId: Long,

    @field:Min(MedicalConstraints.YEAR_MIN.toLong()) @field:NotFutureYear
    @field:Schema(description = "Год постановки диагноза, не позднее текущего", example = "2020")
    val yearOfDiagnosis: Int?
)

data class SurveyResponse(
    @field:Schema(description = "Прогнозы по заболеваниям")
    val predictions: List<DiseasePrediction> = emptyList(),
    val modelVersion: String? = null
)

data class DiseasePrediction(
    val name: String,
    @field:Schema(description = "Вероятность в процентах от 0 до 100", example = "25")
    val probability: Int,  // целое число 0–100
    val modelKind: String? = null,
    val prediction: Int? = null
)

data class GlossaryResponse(
    @field:Schema(description = "Идентификатор записи", example = "1")
    val id: Long,
    val name: String,
    val values: List<GlossaryValueResponse>
)

data class GlossaryValueResponse(
    @field:Schema(description = "Идентификатор записи", example = "1")
    val id: Long,
    val code: Int,
    val value: String
)
