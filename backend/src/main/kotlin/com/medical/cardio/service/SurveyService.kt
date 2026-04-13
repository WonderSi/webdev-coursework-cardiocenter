package com.medical.cardio.service

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.service.MlClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SurveyService(
    private val mlClient: MlClient
) {

    /**
     Принимает данные опроса, вызывает ML для прогноза, возвращает результат. Данные НЕ сохраняются в БД
    */
    fun submitSurvey(request: SurveyRequest): Mono<SurveyResponse> {
        return mlClient.predict(request).map { prediction ->
            SurveyResponse(prediction = prediction)
        }
    }
}
