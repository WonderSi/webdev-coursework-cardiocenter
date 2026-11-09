package com.medical.cardio.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.medical.cardio.exception.GlobalExceptionHandler
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.service.MlClient
import com.medical.cardio.service.SurveyService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.mockito.kotlin.mock
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.reactive.function.client.WebClient

/** Real HTTP calls to ML; no database, training or modification of model artifacts. */
@EnabledIfEnvironmentVariable(named = "ML_INTEGRATION_URL", matches = ".+")
class SurveyMlLiveIntegrationTest {
    @Test
    fun `public survey returns predictions from both real ML models`() {
        val client = MlClient(WebClient.builder(), System.getenv("ML_INTEGRATION_URL"))
        val mvc = MockMvcBuilders.standaloneSetup(
            SurveyController(SurveyService(client), mock<GlossaryRepository>())
        ).setControllerAdvice(GlobalExceptionHandler()).build()
        val mapper = jacksonObjectMapper()
        for ((kind, year) in listOf("city" to 2021, "region" to null)) {
            val body = mapOf(
                "modelKind" to kind, "year" to year, "chronicLungDisease" to 0,
                "genderId" to 1, "age" to 55, "height" to 175, "weight" to 80,
                "alcoholId" to 1, "professionId" to 2, "regionId" to 5,
                "glucose" to 5.7, "cholesterol" to 5.2,
                // Retained public API fields must not reach the strict ML schema.
                "hipMeasurement" to 95, "apolipoproteinA" to 1.4
            )
            mvc.post("/api/survey/submit") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(body)
            }.andExpect {
                status { isOk() }
                jsonPath("$.predictions.length()") { value(1) }
                jsonPath("$.predictions[0].modelKind") { value(kind) }
                jsonPath("$.predictions[0].probability") { isNumber() }
                jsonPath("$.predictions[0].prediction") { isNumber() }
                jsonPath("$.modelVersion") { isNotEmpty() }
            }
        }
    }
}
