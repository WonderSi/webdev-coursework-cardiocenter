package com.medical.cardio.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class UploadReport(
    @field:Schema(description = "Количество строк данных в файле", example = "10")
    val totalRows: Int,
    @field:Schema(description = "Количество сохранённых строк", example = "9")
    val savedRows: Int,
    @field:Schema(description = "Количество пропущенных строк", example = "1")
    val skippedRows: Int,
    @field:Schema(description = "Ошибки отдельных строк")
    val errors: List<RowError>
)

data class RowError(
    @field:Schema(description = "Номер строки исходного файла, включая строку заголовка", example = "2")
    val row: Int,
    @field:Schema(description = "Причина пропуска строки", example = "Некорректный возраст")
    val reason: String
)

data class PatientManualCreateRequest(
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Модель обследования: city или region. Обязательное поле.", allowableValues = ["city", "region"], example = "city")
    val modelKind: String? = null,
    @field:Schema(description = "Для city: 2013, 2017, 2019 или 2021; для region: null", example = "2021")
    val year: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Хронические заболевания лёгких: явный ответ 0 или 1 обязателен", example = "0")
    val chronicLungDisease: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Код пола 1–2, обязательное поле", example = "1")
    val gender: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Возраст, полных лет; обязательное поле", example = "45")
    val age: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Рост, см; обязательное поле", example = "175")
    val height: BigDecimal? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Вес, кг; обязательное поле", example = "80")
    val weight: BigDecimal? = null,
    @field:Schema(description = "Обхват талии/бёдер, см; необязательное поле", example = "95")
    val hipMeasurement: BigDecimal? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Код alcohol 1–3; обязательное поле, как в анкете (ML получает код минус 1)", example = "3")
    val alcohol: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Код справочника profession 1–13 по notebook-13-v1; обязательное поле, не ID БД", example = "2")
    val profession: Int? = null,
    @field:Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Код района region 1–6; обязательное поле, не определяет модель", example = "1")
    val region: Int? = null,
    @field:Schema(description = "Глюкоза, ммоль/л; необязательное неотрицательное значение", example = "5.2")
    val glucose: BigDecimal? = null,
    @field:Schema(description = "Общий холестерин, ммоль/л; необязательное неотрицательное значение", example = "4.8")
    val cholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин ЛПВП, ммоль/л; необязательное неотрицательное значение", example = "1.3")
    val hdlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин ЛПНП, ммоль/л; необязательное неотрицательное значение", example = "2.8")
    val ldlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Триглицериды, ммоль/л; необязательное неотрицательное значение", example = "1.5")
    val triglycerides: BigDecimal? = null
)
