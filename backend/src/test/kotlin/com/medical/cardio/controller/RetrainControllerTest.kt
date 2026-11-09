package com.medical.cardio.controller

import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtAuthFilter
import com.medical.cardio.security.JwtService
import com.medical.cardio.security.ProblemResponseWriter
import com.medical.cardio.service.MlClient
import com.medical.cardio.service.MlRetrainResponse
import com.medical.cardio.service.RetrainService
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.multipart
import org.springframework.mock.web.MockMultipartFile
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import reactor.core.publisher.Mono

@WebMvcTest(RetrainController::class)
@Import(RetrainService::class, SecurityConfig::class, JwtAuthFilter::class, ProblemResponseWriter::class)
@WithMockUser(roles = ["DOCTOR"])
class RetrainControllerTest {
    @BeforeEach
    fun activeModelVersion() {
        whenever(mlClient.getModelVersion()).thenReturn(Mono.just("test-bundle"))
    }

    @Test
    fun `expected retrain failures return problem details with meaningful status`() {
        for ((status, code) in listOf(
            org.springframework.http.HttpStatus.CONFLICT to "ML_RETRAIN_BUSY",
            org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE to "ML_DATASET_TOO_LARGE",
            org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY to "ML_DATASET_INVALID"
        )) {
            whenever(mlClient.retrain(any())).thenReturn(Mono.error(
                com.medical.cardio.exception.MlRetrainRejectedException(status)
            ))
            retrainRequest().andExpect {
                status { isEqualTo(status.value()) }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
                jsonPath("$.code") { value(code) }
                jsonPath("$.instance") { value("/api/admin/retrain") }
            }
        }
    }

    @Test
    fun `dataset schema is forwarded from ML for doctor`() {
        val schema = jacksonObjectMapper().readTree("""{"format":"patients_wide","csv_columns":["id"]}""")
        whenever(mlClient.getDatasetSchema()).thenReturn(Mono.just(schema))
        mvc.get("/api/admin/dataset/schema").andExpect {
            status { isOk() }
            jsonPath("$.format") { value("patients_wide") }
        }
    }

    @Test
    @WithAnonymousUser
    fun `anonymous dataset schema request is denied`() {
        mvc.get("/api/admin/dataset/schema").andExpect { status { isUnauthorized() } }
        verify(mlClient, never()).getDatasetSchema()
    }
    @Autowired lateinit var mvc: MockMvc
    @MockitoBean lateinit var mlClient: MlClient
    @MockitoBean lateinit var jwtService: JwtService
    @MockitoBean lateinit var adminUserDetailsService: AdminUserDetailsService
    @MockitoBean lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `ML metrics failure returns safe problem details`() {
        whenever(mlClient.getMetrics()).thenReturn(Mono.error(failure()))
        mlProblem(mvc.get("/api/admin/model"), "/api/admin/model")
    }

    @Test
    fun `ML retrain failure returns safe problem details`() {
        whenever(mlClient.retrain(any())).thenReturn(Mono.error(failure()))
        mlProblem(retrainRequest(), "/api/admin/retrain")
    }

    @Test
    fun `empty metrics publisher returns ML problem instead of unhandled 500`() {
        whenever(mlClient.getMetrics()).thenReturn(Mono.empty())
        mlProblem(mvc.get("/api/admin/model"), "/api/admin/model")
    }

    @Test
    fun `empty retrain publisher returns ML problem instead of unhandled 500`() {
        whenever(mlClient.retrain(any())).thenReturn(Mono.empty())
        mlProblem(retrainRequest(), "/api/admin/retrain")
    }

    @Test
    fun `empty metrics list cannot describe the active model pair`() {
        whenever(mlClient.getMetrics()).thenReturn(Mono.just(emptyList()))
        mlProblem(mvc.get("/api/admin/model"), "/api/admin/model")
    }

    @Test
    fun `metrics include actual bundle version and validation fields`() {
        val response = jacksonObjectMapper().readValue(
            checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText(), MlRetrainResponse::class.java
        )
        whenever(mlClient.getMetrics()).thenReturn(Mono.just(response.metrics))
        mvc.get("/api/admin/model").andExpect {
            status { isOk() }
            jsonPath("$.modelVersion") { value("test-bundle") }
            jsonPath("$.modelsStats[0].modelStem") { value("city") }
            jsonPath("$.modelsStats[0].validationRows") { value(1185) }
            jsonPath("$.modelsStats[0].threshold") { value(0.5) }
            jsonPath("$.modelsStats[1].modelStem") { value("region") }
        }
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    fun `successful retrain returns models for super admin`() {
        whenever(mlClient.retrain(any())).thenReturn(Mono.just(jacksonObjectMapper().readValue(
            checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText(), MlRetrainResponse::class.java)))
        retrainRequest().andExpect {
            status { isOk() }
            jsonPath("$.status") { value("ok") }
            jsonPath("$.modelVersion") { value("test-bundle") }
            jsonPath("$.inputPatients") { value(2987) }
            jsonPath("$.preparedRows.city") { value(11789) }
            jsonPath("$.modelsStats.length()") { value(2) }
            jsonPath("$.savedModels[0]") { value("city_model.cbm") }
        }
    }

    @Test
    fun `empty dataset never calls ML`() {
        mvc.multipart("/api/admin/retrain") {
            file(MockMultipartFile("dataset", "patients.csv", "text/csv", byteArrayOf()))
        }.andExpect { status { isBadRequest() } }
        verify(mlClient, never()).retrain(any())
    }

    @Test
    fun `missing dataset never calls ML`() {
        mvc.multipart("/api/admin/retrain").andExpect { status { isBadRequest() } }
        verify(mlClient, never()).retrain(any())
    }

    @Test
    @WithAnonymousUser
    fun `anonymous metrics request returns authentication problem`() {
        mvc.get("/api/admin/model").andExpect {
            status { isUnauthorized() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(401) }
            jsonPath("$.code") { value("UNAUTHORIZED") }
        }
        verify(mlClient, never()).getMetrics()
    }

    @Test
    @WithMockUser(roles = ["VIEWER"])
    fun `role without access returns forbidden problem`() {
        retrainRequest().andExpect {
            status { isForbidden() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(403) }
            jsonPath("$.code") { value("FORBIDDEN") }
        }
        verify(mlClient, never()).retrain(any())
    }

    private fun retrainRequest() = mvc.multipart("/api/admin/retrain") {
        file(MockMultipartFile("dataset", "patients.csv", "text/csv", "id,history\n1,0".toByteArray()))
    }

    private fun failure() = MlServiceException(IllegalStateException("http://internal-ml:8000/private-model"))

    private fun mlProblem(result: ResultActionsDsl, path: String) {
        result.andExpect {
            status { isBadGateway() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.type") { value("urn:cardiocenter:problem:ml-service") }
            jsonPath("$.title") { value("Ошибка ML-сервиса") }
            jsonPath("$.status") { value(502) }
            jsonPath("$.detail") { value("Не удалось выполнить операцию ML-сервиса.") }
            jsonPath("$.instance") { value(path) }
            jsonPath("$.code") { value("ML_SERVICE_ERROR") }
            jsonPath("$.errors") { doesNotExist() }
            jsonPath("$.trace") { doesNotExist() }
            content { string(not(containsString("internal-ml"))) }
        }
    }
}
