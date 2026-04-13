package com.medical.cardio.controller

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.service.SurveyService
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
    Контроллер опроса пациента.
    GET /api/survey/config — возвращает конфигурацию опроса (группы вопросов, типы полей, варианты ответов из справочников)
    POST /api/survey/submit — принимает заполненные ответы, вызывает ML для прогноза, возвращает результат
*/
@RestController
@RequestMapping("/api/survey")
class SurveyController(
    private val surveyService: SurveyService,
    private val glossaryRepository: GlossaryRepository
) {

    /*
        Возвращает полную конфигурацию опроса для фронтенда.
    */
    @GetMapping("/config")
    fun getConfig(): ResponseEntity<SurveyConfigResponse> {
        // Загружаем все справочники и группируем по имени
        val glossaries = glossaryRepository.findAllWithValues()
            .associateBy { it.name }

        // Преобразуем шаблоны групп в DTO с подставленными вариантами из справочников
        val groups = SURVEY_GROUPS.map { group ->
            SurveyConfigGroup(
                id = group.id,
                title = group.title,
                requiredFields = group.requiredFields,
                fields = group.fields.map { field ->
                    // Для radio-полей подтягиваем варианты из БД
                    val options = field.glossary?.let { name ->
                        glossaries[name]?.values?.map { v ->
                            SurveyFieldOption(valueId = v.id, label = v.value)
                        }
                    } ?: emptyList()

                    SurveyField(
                        key = field.key,
                        type = field.type,
                        label = field.label,
                        unit = field.unit,
                        placeholder = field.placeholder,
                        horizontal = field.horizontal,
                        vertical = field.vertical,
                        yearKey = field.yearKey,
                        options = options
                    )
                }
            )
        }

        return ResponseEntity.ok(SurveyConfigResponse(groups))
    }

    /*
        Принимает заполненные ответы пациента и возвращает результат ML-прогноза.
        Валидация обязательных полей происходит на фронтенде.
    */
    @PostMapping("/submit")
    fun submitSurvey(@RequestBody @Valid request: SurveyRequest): ResponseEntity<SurveyResponse> =
        surveyService.submitSurvey(request).block()
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.internalServerError().build()

    companion object {
        /*
            Шаблоны групп опроса
            Каждая группа = один шаг опроса на фронтенде
            requiredFields — поля, которые обязательно должны быть заполнены
            glossary — имя справочника в БД для radio-полей (gender, alcohol, profession, region).
        */
        private val SURVEY_GROUPS = listOf(
            SurveyGroupTemplate(
                id = 1,
                title = "Укажите, пожалуйста, ваш пол и возраст:",
                requiredFields = listOf("gender", "age"),
                fields = listOf(
                    SurveyFieldTemplate("gender", "radio", "Пол", glossary = "gender", horizontal = true),
                    SurveyFieldTemplate("age", "number", "Возраст", unit = "лет", placeholder = "27")
                )
            ),
            SurveyGroupTemplate(
                id = 2,
                title = "Укажите, пожалуйста, ваши антропометрические параметры:",
                requiredFields = listOf("height", "weight"),
                fields = listOf(
                    SurveyFieldTemplate("height", "number", "Рост", unit = "см", placeholder = "165"),
                    SurveyFieldTemplate("weight", "number", "Вес", unit = "кг", placeholder = "60"),
                    SurveyFieldTemplate("hipMeasurement", "number", "Объём бёдер", unit = "см", placeholder = "90")
                )
            ),
            SurveyGroupTemplate(
                id = 3,
                title = "Употребляете ли вы алкоголь?",
                requiredFields = listOf("alcohol"),
                fields = listOf(
                    SurveyFieldTemplate("alcohol", "radio", "Алкоголь", glossary = "alcohol", vertical = true)
                )
            ),
            SurveyGroupTemplate(
                id = 4,
                title = "Выберите ваш род деятельности",
                requiredFields = listOf("profession"),
                fields = listOf(
                    SurveyFieldTemplate("profession", "radio", "Профессия", glossary = "profession", vertical = true)
                )
            ),
            SurveyGroupTemplate(
                id = 5,
                title = "Где вы проживаете?",
                requiredFields = listOf("region"),
                fields = listOf(
                    SurveyFieldTemplate("region", "radio", "Район", glossary = "region", vertical = true)
                )
            ),
            SurveyGroupTemplate(
                id = 6,
                title = "Ваши лабораторные параметры",
                requiredFields = emptyList(),
                fields = listOf(
                    SurveyFieldTemplate("glucose", "number", "Глюкоза", unit = "ммоль/л", placeholder = "4,5"),
                    SurveyFieldTemplate("cholesterol", "number", "Холестерин", unit = "ммоль/л", placeholder = "5,3"),
                    SurveyFieldTemplate("nonHdlCholesterol", "number", "Холестерин не-ЛПВП", unit = "ммоль/л", placeholder = "3,2"),
                    SurveyFieldTemplate("vldlCholesterol", "number", "Холестерин ЛПОНП", unit = "ммоль/л", placeholder = "0,8"),
                    SurveyFieldTemplate("hdlCholesterol", "number", "Холестерин ЛПВП", unit = "ммоль/л", placeholder = "1,2"),
                    SurveyFieldTemplate("ldlCholesterol", "number", "Холестерин ЛПНП", unit = "ммоль/л", placeholder = "3,0"),
                    SurveyFieldTemplate("apolipoproteinA", "number", "Аполипопротеин А (Апо А)", unit = "г/л", placeholder = "1,5"),
                    SurveyFieldTemplate("apolipoproteinB", "number", "Аполипопротеин B (Апо B)", unit = "г/л", placeholder = "0,9"),
                    SurveyFieldTemplate("triglycerides", "number", "Триглицериды", unit = "ммоль/л", placeholder = "1,7")
                )
            ),
            SurveyGroupTemplate(
                id = 7,
                title = "Хронические заболевания и диагнозы",
                requiredFields = emptyList(),
                fields = listOf(
                    SurveyFieldTemplate("stroke", "yesno", "Диагностировался ли инсульт?", yearKey = "strokeYear"),
                    SurveyFieldTemplate("heartFailure", "yesno", "Диагностировалась ли сердечная недостаточность (СН)?", yearKey = "heartFailureYear"),
                    SurveyFieldTemplate("cad", "yesno", "Диагностировалось ли нарушение ритма или другие ишемические болезни сердца (ИБС)?", yearKey = "cadYear"),
                    SurveyFieldTemplate("angina", "yesno", "Диагностировалась ли стенокардия?", yearKey = "anginaYear"),
                    SurveyFieldTemplate("myocardialInfarction", "yesno", "Диагностировался ли инфаркт миокарда?", yearKey = "myocardialInfarctionYear"),
                    SurveyFieldTemplate("arterialHypertension", "yesno", "Диагностировалась ли артериальная гипертензия (АГ)?", yearKey = "arterialHypertensionYear")
                )
            )
        )
    }
}

/*
    Шаблон поля опроса — описывает одно поле до обогащения данными из БД.

    @param key — уникальный ключ поля (совпадает с именем в SurveyRequest DTO)
    @param type — тип поля: "radio" (выбор из справочника), "number" (числовой ввод), "yesno" (да/нет)
    @param label — текст метки для отображения на фронтенде
    @param unit — единица измерения (для number-полей)
    @param placeholder — текст-подсказка в поле ввода
    @param glossary — имя справочника в БД (для radio-полей)
    @param horizontal — отображать варианты radio горизонтально
    @param vertical — отображать варианты radio вертикально
    @param yearKey — ключ поля для года (для yesno-полей с уточнением года)
*/
data class SurveyFieldTemplate(
    val key: String,
    val type: String,
    val label: String,
    val unit: String? = null,
    val placeholder: String? = null,
    val glossary: String? = null,
    val horizontal: Boolean = false,
    val vertical: Boolean = false,
    @JsonProperty("yearKey") val yearKey: String? = null
)

/*
    Шаблон группы полей — один шаг опроса.

    @param id — порядковый номер шага
    @param title — заголовок шага
    @param requiredFields — ключи полей, обязательных для заполнения перед переходом к следующему шагу
    @param fields — список полей на этом шаге
*/
data class SurveyGroupTemplate(
    val id: Int,
    val title: String,
    @JsonProperty("requiredFields") val requiredFields: List<String>,
    val fields: List<SurveyFieldTemplate>
)

/*
    GET /api/survey/config.
*/
data class SurveyConfigResponse(
    val groups: List<SurveyConfigGroup>
)

data class SurveyConfigGroup(
    val id: Int,
    val title: String,
    @JsonProperty("requiredFields") val requiredFields: List<String>,
    val fields: List<SurveyField>
)

data class SurveyField(
    val key: String,
    val type: String,
    val label: String,
    val unit: String? = null,
    val placeholder: String? = null,
    val horizontal: Boolean = false,
    val vertical: Boolean = false,
    @JsonProperty("yearKey") val yearKey: String? = null,
    val options: List<SurveyFieldOption> = emptyList()
)

data class SurveyFieldOption(
    @JsonProperty("valueId") val valueId: Long,
    val label: String
)
