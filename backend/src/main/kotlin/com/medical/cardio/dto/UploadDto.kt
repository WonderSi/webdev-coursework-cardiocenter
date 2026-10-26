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
    @field:Schema(description = "Код пола в справочнике gender", example = "1")
    val gender: Int,
    @field:Schema(description = "Возраст, полных лет", example = "45")
    val age: Int,
    @field:Schema(description = "Рост, см", example = "175")
    val height: BigDecimal? = null,
    @field:Schema(description = "Вес, кг", example = "80")
    val weight: BigDecimal? = null,
    @field:Schema(description = "Обхват талии/бёдер, см", example = "95")
    val hipMeasurement: BigDecimal? = null,
    @field:Schema(description = "Код значения справочника alcohol", example = "3")
    val alcohol: Int? = null,
    @field:Schema(description = "Код значения справочника profession", example = "1")
    val profession: Int? = null,
    @field:Schema(description = "Код значения справочника region", example = "1")
    val region: Int? = null,
    @field:Schema(description = "Глюкоза, ммоль/л", example = "5.2")
    val glucose: BigDecimal? = null,
    @field:Schema(description = "Общий холестерин, ммоль/л", example = "4.8")
    val cholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин не-ЛПВП, ммоль/л", example = "3.5")
    val nonHdlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин ЛПОНП, ммоль/л", example = "0.7")
    val vldlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин ЛПВП, ммоль/л", example = "1.3")
    val hdlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Холестерин ЛПНП, ммоль/л", example = "2.8")
    val ldlCholesterol: BigDecimal? = null,
    @field:Schema(description = "Аполипопротеин A", example = "1.4")
    val apolipoproteinA: BigDecimal? = null,
    @field:Schema(description = "Аполипопротеин B", example = "0.9")
    val apolipoproteinB: BigDecimal? = null,
    @field:Schema(description = "Триглицериды, ммоль/л", example = "1.5")
    val triglycerides: BigDecimal? = null,
    @field:Schema(description = "Инсульт: 0 — нет, 1 — есть", example = "0")
    val stroke: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Инсульт", example = "2020")
    val strokeYear: Int? = null,
    @field:Schema(description = "Сердечная недостаточность: 0 — нет, 1 — есть", example = "0")
    val heartFailure: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Сердечная недостаточность", example = "2020")
    val heartFailureYear: Int? = null,
    @field:Schema(description = "Аритмия / ИБС: 0 — нет, 1 — есть", example = "0")
    val cad: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Аритмия / ИБС", example = "2020")
    val cadYear: Int? = null,
    @field:Schema(description = "Стенокардия: 0 — нет, 1 — есть", example = "0")
    val angina: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Стенокардия", example = "2020")
    val anginaYear: Int? = null,
    @field:Schema(description = "Инфаркт миокарда: 0 — нет, 1 — есть", example = "0")
    val myocardialInfarction: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Инфаркт миокарда", example = "2020")
    val myocardialInfarctionYear: Int? = null,
    @field:Schema(description = "Артериальная гипертензия: 0 — нет, 1 — есть", example = "0")
    val arterialHypertension: Int? = null,
    @field:Schema(description = "Год постановки диагноза: Артериальная гипертензия", example = "2020")
    val arterialHypertensionYear: Int? = null,
)
