package com.medical.cardio.controller

import com.medical.cardio.config.SecurityConfig
import com.medical.cardio.exception.MlServiceException
import com.medical.cardio.repository.PatientRepository
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
import reactor.core.publisher.Mono

@WebMvcTest(RetrainController::class)
@Import(RetrainService::class, SecurityConfig::class, JwtAuthFilter::class, ProblemResponseWriter::class)
@WithMockUser(roles = ["DOCTOR"])
class RetrainControllerTest {
    @Autowired lateinit var mvc: MockMvc
    @MockitoBean lateinit var mlClient: MlClient
    @MockitoBean lateinit var patientRepository: PatientRepository
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
        whenever(patientRepository.findAllWithDiagnoses()).thenReturn(emptyList())
        whenever(mlClient.retrain(any())).thenReturn(Mono.error(failure()))
        mlProblem(mvc.post("/api/admin/retrain"), "/api/admin/retrain")
    }

    @Test
    fun `empty metrics publisher returns ML problem instead of unhandled 500`() {
        whenever(mlClient.getMetrics()).thenReturn(Mono.empty())
        mlProblem(mvc.get("/api/admin/model"), "/api/admin/model")
    }

    @Test
    fun `empty retrain publisher returns ML problem instead of unhandled 500`() {
        whenever(patientRepository.findAllWithDiagnoses()).thenReturn(emptyList())
        whenever(mlClient.retrain(any())).thenReturn(Mono.empty())
        mlProblem(mvc.post("/api/admin/retrain"), "/api/admin/retrain")
    }

    @Test
    fun `valid empty metrics list remains a success`() {
        whenever(mlClient.getMetrics()).thenReturn(Mono.just(emptyList()))
        mvc.get("/api/admin/model").andExpect {
            status { isOk() }
            jsonPath("$.modelsStats") { isEmpty() }
            jsonPath("$.savedModels") { isEmpty() }
        }
    }

    @Test
    @WithMockUser(roles = ["SUPER_ADMIN"])
    fun `successful retrain returns models for super admin`() {
        whenever(patientRepository.findAllWithDiagnoses()).thenReturn(emptyList())
        whenever(mlClient.retrain(any())).thenReturn(Mono.just(MlRetrainResponse("ok", listOf("general_model.cbm"))))
        mvc.post("/api/admin/retrain").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("ok") }
            jsonPath("$.savedModels[0]") { value("general_model.cbm") }
        }
    }

    @Test
    fun `dataset failure is a safe internal error and never calls ML`() {
        whenever(patientRepository.findAllWithDiagnoses()).thenThrow(IllegalStateException("jdbc:postgresql://internal-db/private"))
        mvc.post("/api/admin/retrain").andExpect {
            status { isInternalServerError() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(500) }
            jsonPath("$.code") { value("INTERNAL_SERVER_ERROR") }
            jsonPath("$.instance") { value("/api/admin/retrain") }
            jsonPath("$.errors") { doesNotExist() }
            content { string(not(containsString("internal-db"))) }
        }
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
        mvc.post("/api/admin/retrain").andExpect {
            status { isForbidden() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(403) }
            jsonPath("$.code") { value("FORBIDDEN") }
        }
        verify(mlClient, never()).retrain(any())
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
