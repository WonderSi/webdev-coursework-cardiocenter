package com.medical.cardio.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.medical.cardio.dto.DiseasePrediction
import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.exception.MlRetrainRejectedException
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

@Service
class MlClient(
    webClientBuilder: WebClient.Builder,
    @Value("\${app.ml-service.url}") private val mlServiceUrl: String
) {
    private val webClient = webClientBuilder.clone()
        .baseUrl(mlServiceUrl)
        .clientConnector(
            org.springframework.http.client.reactive.ReactorClientHttpConnector(
                reactor.netty.http.client.HttpClient.create()
                    .responseTimeout(java.time.Duration.ofSeconds(10))
            )
        )
        .build()

    private val retrainWebClient = webClientBuilder.clone()
        .baseUrl(mlServiceUrl)
        .clientConnector(
            org.springframework.http.client.reactive.ReactorClientHttpConnector(
                reactor.netty.http.client.HttpClient.create()
                    .responseTimeout(java.time.Duration.ofMinutes(15))
            )
        )
        .build()

    fun predict(request: SurveyRequest): Mono<SurveyResponse> {
        val mlRequest = MlPredictRequest(
            modelKind = request.modelKind,
            year = request.year,
            chronicLungDisease = request.chronicLungDisease,
            gender = request.genderId?.toInt(),
            age = request.age,
            height = request.height?.toDouble(),
            weight = request.weight?.toDouble(),
            alcohol = request.alcoholId?.toInt()?.minus(1),
            profession = request.professionId?.toInt(),
            region = request.regionId?.toInt(),
            glucose = request.glucose?.toDouble(),
            cholesterol = request.cholesterol?.toDouble(),
            hdlCholesterol = request.hdlCholesterol?.toDouble(),
            ldlCholesterol = request.ldlCholesterol?.toDouble(),
            triglycerides = request.triglycerides?.toDouble(),
            stroke = request.stroke,
            strokeYear = request.strokeYear,
            heartFailure = request.heartFailure,
            heartFailureYear = request.heartFailureYear,
            cadChdIhd = request.cad,
            cadChdIhdYear = request.cadYear,
            angine = request.angina,
            angineYear = request.anginaYear,
            myocardialInfarction = request.myocardialInfarction,
            myocardialInfarctionYear = request.myocardialInfarctionYear,
            arterialHypertension = request.arterialHypertension,
            arterialHypertensionYear = request.arterialHypertensionYear,
        )

        return webClient.post()
            .uri("/predictions/predict")
            .bodyValue(mlRequest)
            .retrieve()
            .bodyToMono(MlPredictResponse::class.java)
            .switchIfEmpty(Mono.error(IllegalStateException("Пустой ответ ML-сервиса")))
            .map {
                check(it.predictions.size == 1 && it.predictions.single().disease == request.modelKind) {
                    "ML response does not match the requested model"
                }
                check(it.modelVersion.isNotBlank()) { "Missing model version" }
                SurveyResponse(parsePredictions(it.predictions), it.modelVersion)
            }
            .onErrorMap { MlServiceException(it) }
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
            .switchIfEmpty(Mono.error(IllegalStateException("Пустой ответ ML-сервиса")))
            .map { response ->
                check(response.status == "ok" && response.modelVersion.isNotBlank()) { "Invalid retrain result" }
                check(response.savedModels.size == 2 && response.savedModels.toSet() == setOf("city_model.cbm", "region_model.cbm")) { "Incomplete model pair" }
                check(response.inputPatients > 0 && response.preparedRows.keys == setOf("city", "region") && response.preparedRows.values.all { it > 0 }) { "Invalid dataset counts" }
                check(response.metrics.size == 2 && response.metrics.map { it.modelStem }.toSet() == setOf("city", "region")) { "Missing model metrics" }
                response
            }
            .onErrorMap { error ->
                if (error is WebClientResponseException && error.statusCode.value() in setOf(409, 413, 422)) {
                    MlRetrainRejectedException(HttpStatus.valueOf(error.statusCode.value()))
                } else MlServiceException(error)
            }
    }

    fun getDatasetSchema(): Mono<com.fasterxml.jackson.databind.JsonNode> = webClient.get()
        .uri("/dataset/schema")
        .retrieve()
        .bodyToMono(com.fasterxml.jackson.databind.JsonNode::class.java)
        .switchIfEmpty(Mono.error(IllegalStateException("Empty ML dataset schema")))
        .onErrorMap { MlServiceException(it) }

    fun getModelVersion(): Mono<String> = webClient.get()
        .uri("/health")
        .retrieve()
        .bodyToMono(MlHealthResponse::class.java)
        .switchIfEmpty(Mono.error(IllegalStateException("Empty ML health response")))
        .map { response ->
            val version = response.modelVersion
            check(response.status == "ok" && !version.isNullOrBlank() && response.models.toSet() == setOf("city", "region")) {
                "ML model pair is unavailable"
            }
            version
        }
        .onErrorMap { MlServiceException(it) }

    fun getMetrics(): Mono<List<MlMetric>> =
        webClient.get()
            .uri("/metrics")
            .retrieve()
            .bodyToMono(Array<MlMetric>::class.java)
            .switchIfEmpty(Mono.error(IllegalStateException("Пустой ответ ML-сервиса")))
            .map { it.toList() }
            .onErrorMap { MlServiceException(it) }

    private fun parsePredictions(rawList: List<MlDiseasePrediction>): List<DiseasePrediction> =
        rawList.map { item ->
            check(item.probability.isFinite() && item.probability in 0.0..1.0) { "Invalid probability" }
            check(item.prediction in 0..1) { "Invalid prediction" }
            DiseasePrediction(
                name = DISEASE_NAMES.getValue(item.disease),
                probability = (item.probability * 100).toInt(),
                modelKind = item.disease,
                prediction = item.prediction
            )
        }

    companion object {
        val DISEASE_NAMES = mapOf(
            "city" to "Составной сердечно-сосудистый исход обследования (city)",
            "region" to "Сердечно-сосудистый исход за период наблюдения (region)",
        )
    }
}

data class MlPredictRequest(
    @JsonProperty("model_kind") val modelKind: String?,
    val year: Int?,
    @JsonProperty("chronic_lung_disease") val chronicLungDisease: Int?,
    val gender: Int?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val alcohol: Int?,
    val profession: Int?,
    val region: Int?,
    val glucose: Double?,
    val cholesterol: Double?,
    @JsonProperty("hdl_cholesterol") val hdlCholesterol: Double?,
    @JsonProperty("ldl_cholesterol") val ldlCholesterol: Double?,
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
    val probability: Double,
    val prediction: Int
)

data class MlPredictResponse(
    val predictions: List<MlDiseasePrediction>,
    @JsonProperty("model_version") val modelVersion: String
)

data class MlRetrainResponse(
    val status: String,
    @JsonProperty("saved_models") val savedModels: List<String>,
    @JsonProperty("model_version") val modelVersion: String,
    val metrics: List<MlMetric>,
    @JsonProperty("input_patients") val inputPatients: Int,
    @JsonProperty("prepared_rows") val preparedRows: Map<String, Int>
)

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
data class MlMetric(
    val target: String? = null,
    val iterations: Int? = null,
    val threshold: Double? = null,
    @JsonProperty("model_variant") val modelVariant: String? = null,
    @JsonProperty("train_patients") val trainPatients: Int? = null,
    @JsonProperty("validation_rows") val validationRows: Int? = null,
    @JsonProperty("validation_patients") val validationPatients: Int? = null,
    @JsonProperty("validation_pos") val validationPos: Int? = null,
    @JsonProperty("validation_neg") val validationNeg: Int? = null,
    @JsonProperty("test_patients") val testPatients: Int? = null,
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


data class MlHealthResponse(
    val status: String,
    val models: List<String>,
    @JsonProperty("model_version") val modelVersion: String? = null
)
