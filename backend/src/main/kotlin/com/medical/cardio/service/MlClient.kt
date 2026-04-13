package com.medical.cardio.service

import com.medical.cardio.dto.PredictionResult
import com.medical.cardio.dto.SurveyRequest
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
    private val webClient = webClientBuilder.baseUrl(mlServiceUrl).build()

    fun predict(request: SurveyRequest): Mono<PredictionResult> {
        log.info("ML prediction called — genderId={}, age={}", request.genderId, request.age)

        // Преобразование в формат, ожидаемый ML-сервисом
        val mlRequest = MlPredictRequest(
            gender = request.genderId?.toInt(),
            age = request.age,
            height = request.height?.toDouble(),
            weight = request.weight?.toDouble(),
            hip_measurement = request.hipMeasurement?.toDouble(),
            alcohol = request.alcoholId?.toInt(),
            profession = request.professionId?.toInt(),
            region = request.regionId?.toInt(),
            glucose = request.glucose?.toDouble(),
            cholesterol = request.cholesterol?.toDouble(),
            non_hdl_cholesterol = request.nonHdlCholesterol?.toDouble(),
            vldl_cholesterol = request.vldlCholesterol?.toDouble(),
            hdl_cholesterol = request.hdlCholesterol?.toDouble(),
            ldl_cholesterol = request.ldlCholesterol?.toDouble(),
            apolipoprotein_a = request.apolipoproteinA?.toDouble(),
            apolipoprotein_b = request.apolipoproteinB?.toDouble(),
            triglycerides = request.triglycerides?.toDouble(), 
            stroke = request.stroke?.let { if (it) 1 else 0 }, // true=1, false=0
            stroke_year = request.strokeYear,
            heart_failure = request.heartFailure?.let { if (it) 1 else 0 },
            heart_failure_year = request.heartFailureYear,
            cad_chd_ihd = request.cad?.let { if (it) 1 else 0 },
            cad_chd_ihd_year = request.cadYear,
            angine = request.angina?.let { if (it) 1 else 0 },
            angine_year = request.anginaYear,
            myocardial_infarction = request.myocardialInfarction?.let { if (it) 1 else 0 },
            myocardial_infarction_year = request.myocardialInfarctionYear,
            arterial_hypertension = request.arterialHypertension?.let { if (it) 1 else 0 },
            arterial_hypertension_year = request.arterialHypertensionYear
        )

        return webClient.post()
            .uri("/predictions/predict")
            .bodyValue(mlRequest)
            .retrieve()
            .bodyToMono(MlPredictResponse::class.java)
            .map { response ->
                PredictionResult(
                    diseaseType = response.disease_type,
                    probability = "${(response.disease_probability * 100).toInt()}%", // Преобразование в проценты
                    message = response.message
                )
            }
            .onErrorResume { e ->
                log.error("ML service call failed: ${e.message}")
                Mono.just(
                    PredictionResult(
                        diseaseType = null,
                        probability = null,
                        message = "Сервис прогнозирования временно недоступен. Попробуйте позже."
                    )
                )
            }
    }
}

data class MlPredictRequest(
    val gender: Int?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val hip_measurement: Double?,
    val alcohol: Int?,
    val profession: Int?,
    val region: Int?,
    val glucose: Double?,
    val cholesterol: Double?,
    val non_hdl_cholesterol: Double?,
    val vldl_cholesterol: Double?,
    val hdl_cholesterol: Double?,
    val ldl_cholesterol: Double?,
    val apolipoprotein_a: Double?,
    val apolipoprotein_b: Double?,
    val triglycerides: Double?,
    val stroke: Int?,
    val stroke_year: Int?,
    val heart_failure: Int?,
    val heart_failure_year: Int?,
    val cad_chd_ihd: Int?,
    val cad_chd_ihd_year: Int?,
    val angine: Int?,
    val angine_year: Int?,
    val myocardial_infarction: Int?,
    val myocardial_infarction_year: Int?,
    val arterial_hypertension: Int?,
    val arterial_hypertension_year: Int?
)

data class MlPredictResponse(
    val disease_type: String?,
    val disease_probability: Double,
    val message: String,
    val model_version: String
)
