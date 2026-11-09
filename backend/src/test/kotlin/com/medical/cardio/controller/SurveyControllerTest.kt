package com.medical.cardio.controller

import com.medical.cardio.dto.DiseasePrediction
import com.medical.cardio.dto.SurveyResponse
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.service.MlClient
import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.JwtService
import com.medical.cardio.security.ProblemResponseWriter
import com.medical.cardio.service.SurveyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.test.web.servlet.MockMvc
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import reactor.core.publisher.Mono


@WebMvcTest(SurveyController::class)
@Import(SurveyService::class, SecurityConfig::class, JwtAuthFilter::class, ProblemResponseWriter::class)
class SurveyControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @MockitoBean private lateinit var glossaryRepository: GlossaryRepository
    @MockitoBean private lateinit var jwtService: JwtService
    @MockitoBean private lateinit var adminUserDetailsService: AdminUserDetailsService
    @MockitoBean private lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `new model fields are validated before calling ML`() {
        val mapper = jacksonObjectMapper()
        val valid = mapper.readTree(validSurveyJson)
        for (overrides in listOf(
            """{"modelKind":"city","year":null}""",
            """{"modelKind":"city","year":2026}""",
            """{"modelKind":"region","year":2021}""",
            """{"modelKind":"general"}""",
            """{"chronicLungDisease":null}""",
            """{"professionId":14}"""
        )) {
            val body = (valid.deepCopy() as com.fasterxml.jackson.databind.node.ObjectNode)
                .setAll<com.fasterxml.jackson.databind.node.ObjectNode>(mapper.readTree(overrides) as com.fasterxml.jackson.databind.node.ObjectNode)
            mockMvc.post("/api/survey/submit") {
                contentType = MediaType.APPLICATION_JSON
                content = body.toString()
            }.andExpect { status { isBadRequest() } }
        }
        verify(mlClient, never()).predict(any())
    }

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
          "modelKind": "region",
          "chronicLungDisease": 0,
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
            Mono.just(SurveyResponse(listOf(DiseasePrediction(name = "Общий риск", probability = 72)), "test-version"))
        )

        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson
        }.andExpect {
            status { isOk() }
            jsonPath("$.predictions[0].name") { value("Общий риск") }
            jsonPath("$.predictions[0].probability") { value(72) }
            jsonPath("$.modelVersion") { value("test-version") }
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
            content = validSurveyJson.replace("\"age\": 45", "\"age\": 10")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `submit with height out of range returns 400`() {
        mockMvc.post("/api/survey/submit") {
            contentType = MediaType.APPLICATION_JSON
            content = validSurveyJson.replace("\"height\": 175.0", "\"height\": 10.0")
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
