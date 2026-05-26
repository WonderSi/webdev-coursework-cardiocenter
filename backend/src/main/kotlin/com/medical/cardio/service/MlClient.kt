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

    fun getMetrics(): Mono<List<MlMetric>> =
        webClient.get()
            .uri("/metrics")
            .retrieve()
            .bodyToFlux(MlMetric::class.java)
            .collectList()
            .onErrorResume { e ->
                log.error("ML metrics fetch failed: ${e.message}")
                Mono.error(RuntimeException("Ошибка получения метрик: ${e.message}"))
            }

    private fun parsePredictions(rawList: List<MlDiseasePrediction>): List<DiseasePrediction> {
        val parsed = rawList.map { item ->
            DiseasePrediction(
                name = DISEASE_NAMES[item.disease] ?: item.disease,
                probability = (item.probability * 100).toInt().coerceIn(0, 100)
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

data class MlDiseasePrediction(
    val disease: String,
    val probability: Double
)

data class MlPredictResponse(
    val predictions: List<MlDiseasePrediction>,
    @JsonProperty("model_version") val modelVersion: String
)

data class MlRetrainResponse(
    val status: String,
    @JsonProperty("saved_models") val savedModels: List<String> = emptyList(),
    @JsonProperty("models_dir") val modelsDir: String? = null,
    @JsonProperty("metrics_json") val metricsJson: String? = null,
    @JsonProperty("metrics_csv") val metricsCsv: String? = null,
)

data class MlMetric(
    @JsonProperty("model_stem") val modelStem: String,
    @JsonProperty("feature_count") val featureCount: Int,
    @JsonProperty("train_rows") val trainRows: Int,
    @JsonProperty("test_rows") val testRows: Int,
    @JsonProperty("train_pos") val trainPos: Int,
    @JsonProperty("train_neg") val trainNeg: Int,
    @JsonProperty("test_pos") val testPos: Int,
    @JsonProperty("test_neg") val testNeg: Int,
    @JsonProperty("class_weight_0") val classWeight0: Double,
    @JsonProperty("class_weight_1") val classWeight1: Double,
    @JsonProperty("roc_auc") val rocAuc: Double,
    @JsonProperty("pr_auc") val prAuc: Double,
    val accuracy: Double,
    @JsonProperty("balanced_accuracy") val balancedAccuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1: Double,
    val logloss: Double,
    val brier: Double,
    val tn: Int,
    val fp: Int,
    val fn: Int,
    val tp: Int,
)

