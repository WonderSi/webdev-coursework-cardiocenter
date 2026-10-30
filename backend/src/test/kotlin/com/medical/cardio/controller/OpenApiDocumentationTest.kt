package com.medical.cardio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.config.OpenApiConfig
import com.medical.cardio.config.ProblemOpenApiConfig
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtService
import com.medical.cardio.service.*
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.test.context.ActiveProfiles
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.get

@SpringBootTest(classes = [OpenApiDocumentationTest.DocumentationApplication::class])
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("openapi-documentation")
@TestPropertySource(properties = [
    "springdoc.default-produces-media-type=application/json",
    "springdoc.packages-to-scan=com.medical.cardio.controller",
    "springdoc.paths-to-match=/api/**",
    "springdoc.override-with-generic-response=false"
])
class OpenApiDocumentationTest {
    // Documentation generation needs controller signatures, not a database or a running ML service.
    @Configuration
    @Profile("openapi-documentation")
    @EnableAutoConfiguration(exclude = [
        DataSourceAutoConfiguration::class, HibernateJpaAutoConfiguration::class,
        FlywayAutoConfiguration::class,
        UserDetailsServiceAutoConfiguration::class
    ])
    @Import(
        OpenApiConfig::class, ProblemOpenApiConfig::class, AdminController::class, AuthController::class,
        DashboardController::class, PatientController::class, RetrainController::class,
        SurveyController::class, UploadController::class
    )
    class DocumentationApplication

    @MockitoBean lateinit var adminService: AdminService
    @MockitoBean lateinit var dashboardService: DashboardService
    @MockitoBean lateinit var uploadService: UploadService
    @MockitoBean lateinit var retrainService: RetrainService
    @MockitoBean lateinit var surveyService: SurveyService
    @MockitoBean lateinit var glossaryRepository: GlossaryRepository
    @MockitoBean lateinit var authenticationManager: AuthenticationManager
    @MockitoBean lateinit var jwtService: JwtService
    @MockitoBean lateinit var userDetailsService: AdminUserDetailsService

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `documentation describes public and protected operations and real response shapes`() {
        val result = mockMvc.get("/v3/api-docs").andExpect { status { isOk() } }.andReturn()
        val document = objectMapper.readTree(result.response.contentAsString)
        val paths = document.path("paths")
        val scheme = document.at("/components/securitySchemes/cookieAuth")
        assertEquals("apiKey", scheme.path("type").asText())
        assertEquals("cookie", scheme.path("in").asText())
        assertEquals("access_token", scheme.path("name").asText())

        var operationCount = 0
        paths.fields().forEachRemaining { (path, methods) ->
            methods.fields().forEachRemaining { (_, operation) ->
                operationCount++
                assertTrue(operation.path("summary").asText().isNotBlank(), path)
                val protected = path.startsWith("/api/admin/") || path == "/api/auth/me"
                assertEquals(protected, operation.path("security").any { it.has("cookieAuth") }, path)
            }
        }
        assertEquals(14, operationCount)
        val doctors = paths.path("/api/admin/super-admin/doctors")
        val created = doctors.path("post").at("/responses/201/content/application~1json/schema")
        assertEquals("#/components/schemas/DoctorResponse", created.path("\$ref").asText())
        assertEquals("array", doctors.path("get").at("/responses/200/content/application~1json/schema/type").asText())
        assertTrue(document.at("/components/schemas/DoctorResponse/properties").has("isBlocked"))
        assertTrue(document.at("/components/schemas/CreateDoctorRequest/properties/password/writeOnly").asBoolean())
        val blocked = paths.path("/api/admin/super-admin/doctors/{id}/blocked").path("put")
        assertFalse(blocked.at("/responses/204").has("content"))
        assertEquals("#/components/responses/Unauthorized",
            paths.path("/api/auth/login").path("post").at("/responses/401/\$ref").asText())
        assertFalse(paths.path("/api/auth/me").path("get").has("requestBody"))
        assertEquals("binary", paths.path("/api/admin/upload/template").path("get")
            .at("/responses/200/content/application~1octet-stream/schema/format").asText())
    }

    @Test
    fun `all documented errors use the shared problem contract`() {
        val result = mockMvc.get("/v3/api-docs").andExpect { status { isOk() } }.andReturn()
        val document = objectMapper.readTree(result.response.contentAsString)
        val schema = document.at("/components/schemas/ApiProblem")
        assertTrue(schema.path("required").map { it.asText() }.containsAll(
            listOf("type", "title", "status", "detail", "instance", "code")))
        assertFalse(schema.path("required").any { it.asText() == "errors" })
        document.path("paths").fields().forEachRemaining { (path, methods) ->
            methods.fields().forEachRemaining { (_, operation) ->
                assertTrue(operation.path("responses").has("500"), path)
                operation.path("responses").fields().forEachRemaining { (status, response) ->
                    if (status.toIntOrNull()?.let { it >= 400 } == true) {
                        val ref = response.path("\$ref").asText()
                        assertTrue(ref.startsWith("#/components/responses/"), "$path $status")
                        val resolved = document.at(ref.removePrefix("#"))
                        val content = resolved.path("content")
                        assertEquals(listOf("application/problem+json"), content.fieldNames().asSequence().toList())
                        assertEquals("#/components/schemas/ApiProblem",
                            content.path("application/problem+json").at("/schema/\$ref").asText())
                    }
                }
            }
        }
        listOf("/api/survey/submit", "/api/admin/retrain", "/api/admin/model").forEach { path ->
            val operation = document.path("paths").path(path).elements().next()
            assertEquals("#/components/responses/MlServiceError", operation.at("/responses/502/\$ref").asText())
        }
        val validation = document.at("/components/responses/BadRequest/content/application~1problem+json/examples/validation/value")
        assertEquals("VALIDATION_ERROR", validation.path("code").asText())
        assertTrue(validation.path("errors").isArray)
        assertFalse(document.toString().contains("200 с пустым predictions"))
    }
}
