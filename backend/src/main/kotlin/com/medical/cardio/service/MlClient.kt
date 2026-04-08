package com.medical.cardio.service

import com.medical.cardio.dto.PredictionResult
import com.medical.cardio.dto.SurveyRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class MlClient {
    // Пока только mock данные, позже обновлю и присоеденю реальный ML сервис
    private val log = LoggerFactory.getLogger(javaClass)

    fun predict(request: SurveyRequest): Mono<PredictionResult> {
        log.info("ML prediction called — mock mode")
        return Mono.just(MOCK_RESULT)
    }
    // Как бы готово, но бесполезно без админ панели
    // fun retrain(): Mono<Unit> {
    //     log.info("ML retrain called — mock mode")
    //     return Mono.just(Unit)
    // }

    companion object {
        private val MOCK_RESULT = PredictionResult(
            diseaseType = "Артериальная гипертензия",
            probability = "67%",
            message = "По результатам опроса у вас повышенный риск развития сердечно-сосудистых заболеваний (АГ). Рекомендуем обратиться к кардиологу."
        )
    }
}
