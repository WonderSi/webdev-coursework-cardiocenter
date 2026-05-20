package com.medical.cardio.controller

import com.medical.cardio.dto.DiseasePrediction
import com.medical.cardio.service.MlClient
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import reactor.core.publisher.Mono

class SurveyControllerTest : ControllerTestBase() {

    @MockBean
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
    fun `submit when ML service unavailable returns 200 with empty predictions`() {
        whenever(mlClient.predict(any())).thenReturn(Mono.just(emptyList()))

        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson
        }.andExpect {
            status { isOk() }
            jsonPath("$.predictions") { isArray() }
        }
    }
}
