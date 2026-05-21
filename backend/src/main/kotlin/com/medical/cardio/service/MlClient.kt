package com.medical.cardio.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.medical.cardio.dto.DiseasePrediction
import com.medical.cardio.dto.SurveyRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class MlClient(
    webClientBuilder: WebClient.Builder,
    @Value("\${app.ml-service.url}") private val mlServiceUrl: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val webClient = webClientBuilder
        .baseUrl(mlServiceUrl)
        .clientConnector(
            org.springframework.http.client.reactive.ReactorClientHttpConnector(
                reactor.netty.http.client.HttpClient.create()
                    .responseTimeout(java.time.Duration.ofSeconds(10))
            )
        )
        .build()

    private val retrainWebClient = webClientBuilder
        .baseUrl(mlServiceUrl)
        .clientConnector(
            org.springframework.http.client.reactive.ReactorClientHttpConnector(
                reactor.netty.http.client.HttpClient.create()
                    .responseTimeout(java.time.Duration.ofMinutes(15))
            )
        )
        .build()

    fun predict(request: SurveyRequest): Mono<List<DiseasePrediction>> {
        log.info("ML prediction called — genderId={}, age={}", request.genderId, request.age)

        val mlRequest = MlPredictRequest(
            gender = request.genderId?.toInt(),
            age = request.age,
            height = request.height?.toDouble(),
            weight = request.weight?.toDouble(),
            hipMeasurement = request.hipMeasurement?.toDouble() ?: 0.0,
            alcohol = request.alcoholId?.toInt()?.minus(1),
            profession = request.professionId?.toInt(),
            region = request.regionId?.toInt(),
            glucose = request.glucose?.toDouble(),
            cholesterol = request.cholesterol?.toDouble(),
            nonHdlCholesterol = request.nonHdlCholesterol?.toDouble(),
            vldlCholesterol = request.vldlCholesterol?.toDouble(),
            hdlCholesterol = request.hdlCholesterol?.toDouble(),
            ldlCholesterol = request.ldlCholesterol?.toDouble(),
            apolipoproteinA = request.apolipoproteinA?.toDouble(),
            apolipoproteinB = request.apolipoproteinB?.toDouble(),
            triglycerides = request.triglycerides?.toDouble(),
            stroke = request.stroke,
            strokeYear = request.strokeYear ?: 1950,
            heartFailure = request.heartFailure,
            heartFailureYear = request.heartFailureYear ?: 1950,
            cadChdIhd = request.cad,
            cadChdIhdYear = request.cadYear ?: 1950,
            angine = request.angina,
            angineYear = request.anginaYear ?: 1950,
            myocardialInfarction = request.myocardialInfarction,
            myocardialInfarctionYear = request.myocardialInfarctionYear ?: 1950,
            arterialHypertension = request.arterialHypertension,
            arterialHypertensionYear = request.arterialHypertensionYear ?: 1950,
        )

        log.info("Sending ML request: {}", mlRequest)

        return webClient.post()
            .uri("/predictions/predict")
            .bodyValue(mlRequest)
            .retrieve()
            .bodyToMono(MlPredictResponse::class.java)
            .map { parsePredictions(it.predictions) }
            .onErrorResume { e ->
                log.error("ML service call failed: ${e.message}")
                if (e is org.springframework.web.reactive.function.client.WebClientResponseException) {
                    log.error("ML response body: ${e.responseBodyAsString}")
                }
                Mono.just(emptyList())
            }
    }

    fun retrain(csvBytes: ByteArray): Mono<MlRetrainResponse> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType("text/csv; charset=utf-8")
            setContentDispositionFormData("dataset", "patients.csv")
        }
        val multipart = LinkedMultiValueMap<String, Any>().apply {
            add("dataset", HttpEntity(csvBytes, headers))
        }

        return retrainWebClient.post()
            .uri("/retrain")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipart))
            .retrieve()
            .bodyToMono(MlRetrainResponse::class.java)
            .onErrorResume { e ->
                log.error("ML retrain failed: ${e.message}")
                if (e is org.springframework.web.reactive.function.client.WebClientResponseException) {
                    log.error("ML response body: ${e.responseBodyAsString}")
                }
                Mono.error(RuntimeException("Ошибка переобучения модели: ${e.message}"))
            }
    }

    private fun parsePredictions(rawList: List<String>): List<DiseasePrediction> {
        val parsed = rawList.mapNotNull { item ->
            val idx = item.indexOf(':')
            if (idx < 0) return@mapNotNull null
            val key = item.substring(0, idx).trim()
            val prob = item.substring(idx + 1).trim().toDoubleOrNull() ?: return@mapNotNull null
            DiseasePrediction(
                name = DISEASE_NAMES[key] ?: key,
                probability = (prob * 100).toInt().coerceIn(0, 100)
            )
        }
        val general = parsed.filter { it.name == DISEASE_NAMES["general"] }
        val others  = parsed.filter { it.name != DISEASE_NAMES["general"] }
            .sortedByDescending { it.probability }
        return general + others
    }

    companion object {
        val DISEASE_NAMES = mapOf(
            "general"           to "Общий риск",
            "ah"                to "Артериальная гипертензия",
            "angina"            to "Стенокардия",
            "arrhythmia_or_ihd" to "Нарушение ритма / ИБС",
            "heart_failure"     to "Сердечная недостаточность",
            "mi"                to "Инфаркт миокарда",
            "stroke"            to "Инсульт",
        )
    }
}

data class MlPredictRequest(
    val gender: Int?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    @JsonProperty("hip_measurement") val hipMeasurement: Double?,
    val alcohol: Int?,
    val profession: Int?,
    val region: Int?,
    val glucose: Double?,
    val cholesterol: Double?,
    @JsonProperty("non_hdl_cholesterol") val nonHdlCholesterol: Double?,
    @JsonProperty("vldl_cholesterol") val vldlCholesterol: Double?,
    @JsonProperty("hdl_cholesterol") val hdlCholesterol: Double?,
    @JsonProperty("ldl_cholesterol") val ldlCholesterol: Double?,
    @JsonProperty("apolipoprotein_a") val apolipoproteinA: Double?,
    @JsonProperty("apolipoprotein_b") val apolipoproteinB: Double?,
    val triglycerides: Double?,
    val stroke: Int?,
    @JsonProperty("stroke_year") val strokeYear: Int?,
    @JsonProperty("heart_failure") val heartFailure: Int?,
    @JsonProperty("heart_failure_year") val heartFailureYear: Int?,
    @JsonProperty("cad_chd_ihd") val cadChdIhd: Int?,
    @JsonProperty("cad_chd_ihd_year") val cadChdIhdYear: Int?,
    val angine: Int?,
    @JsonProperty("angine_year") val angineYear: Int?,
    @JsonProperty("myocardial_infarction") val myocardialInfarction: Int?,
    @JsonProperty("myocardial_infarction_year") val myocardialInfarctionYear: Int?,
    @JsonProperty("arterial_hypertension") val arterialHypertension: Int?,
    @JsonProperty("arterial_hypertension_year") val arterialHypertensionYear: Int?
)

data class MlPredictResponse(
    val predictions: List<String>,
    @JsonProperty("model_version") val modelVersion: String
)

data class MlRetrainResponse(
    val status: String,
    @JsonProperty("saved_models") val savedModels: List<String> = emptyList(),
    @JsonProperty("models_dir") val modelsDir: String? = null,
    @JsonProperty("metrics_json") val metricsJson: String? = null,
    @JsonProperty("metrics_csv") val metricsCsv: String? = null,
)