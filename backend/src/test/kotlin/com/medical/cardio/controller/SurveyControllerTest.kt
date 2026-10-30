package com.medical.cardio.controller

import com.medical.cardio.dto.DiseasePrediction
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.service.MlClient
import com.medical.cardio.support.ControllerTestBase
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import reactor.core.publisher.Mono


class SurveyControllerTest : ControllerTestBase() {

    @Test
    fun `empty ML publisher returns problem details instead of empty 500`() {
        whenever(mlClient.predict(any())).thenReturn(Mono.empty())
        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson
        }.andExpect {
            status { isBadGateway() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(502) }
            jsonPath("$.code") { value("ML_SERVICE_ERROR") }
            jsonPath("$.instance") { value("/api/survey/submit") }
        }
    }

    @MockitoBean
    private lateinit var mlClient: MlClient

    private val validSurveyJson = """
        {
          "genderId": 1,
          "age": 45,
          "height": 175.0,
          "weight": 80.0,
          "alcoholId": 1,
          "professionId": 1,
          "regionId": 1
        }
    """.trimIndent()

    @Test
    fun `submit valid survey returns 200 with predictions`() {
        whenever(mlClient.predict(any())).thenReturn(
            Mono.just(listOf(DiseasePrediction(name = "Общий риск", probability = 72)))
        )

        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson
        }.andExpect {
            status { isOk() }
            jsonPath("$.predictions[0].name") { value("Общий риск") }
            jsonPath("$.predictions[0].probability") { value(72) }
        }
    }

    @Test
    fun `submit with missing required fields returns 400`() {
        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"age": 45}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `submit with age below minimum returns 400`() {
        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"genderId":1,"age":10,"height":175.0,"weight":80.0,"alcoholId":1,"professionId":1,"regionId":1}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `submit with height out of range returns 400`() {
        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"genderId":1,"age":45,"height":10.0,"weight":80.0,"alcoholId":1,"professionId":1,"regionId":1}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `submit when ML service unavailable returns 502 with problem details`() {
        val cause = RuntimeException("Connection failed: http://internal-ml:8000")

        whenever(mlClient.predict(any())).thenReturn(
            Mono.error(MlServiceException(cause))
        )

        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson
        }.andExpect {
            status { isBadGateway() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:cardiocenter:problem:ml-service") }
            jsonPath("$.title") { value("Ошибка ML-сервиса") }
            jsonPath("$.status") { value(502) }
            jsonPath("$.detail") { value("Не удалось выполнить операцию ML-сервиса.") }
            jsonPath("$.instance") { value("/api/survey/submit") }
            jsonPath("$.code") { value("ML_SERVICE_ERROR") }
            jsonPath("$.errors") { doesNotExist() }
            jsonPath("$.predictions") { doesNotExist() }
            content { string(Matchers.not(Matchers.containsString("internal-ml"))) }
        }
    }
}
