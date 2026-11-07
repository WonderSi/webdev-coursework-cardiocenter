package com.medical.cardio.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.config.OpenApiConfig
import com.medical.cardio.config.ProblemOpenApiConfig
import com.medical.cardio.repository.GlossaryRepository
import com.medical.cardio.security.AuthCookieService
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
    @MockitoBean lateinit var authService: AuthService
    @MockitoBean lateinit var authCookieService: AuthCookieService
    @MockitoBean lateinit var loginRateLimitService: LoginRateLimitService

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
        assertEquals(16, operationCount)
        assertEquals("refresh_token", document.at("/components/securitySchemes/refreshCookieAuth/name").asText())
        val refresh = paths.path("/api/auth/refresh").path("post")
        assertTrue(refresh.path("security").any { it.has("refreshCookieAuth") })
        assertFalse(refresh.has("requestBody"))
        assertTrue(refresh.path("parameters").any { it.path("name").asText() == "X-XSRF-TOKEN" })
        assertEquals("#/components/responses/InvalidRefresh", refresh.at("/responses/401/\$ref").asText())
        assertEquals("#/components/responses/CsrfRejected", refresh.at("/responses/403/\$ref").asText())
        assertTrue(paths.path("/api/auth/csrf").has("get"))
        val logout = paths.path("/api/auth/logout").path("post")
        assertTrue(logout.path("parameters").any { it.path("name").asText() == "X-XSRF-TOKEN" })
        assertEquals("#/components/responses/CsrfRejected", logout.at("/responses/403/\$ref").asText())
        assertFalse(logout.path("responses").has("401"))
        assertFalse(logout.path("responses").path("200").has("content"))
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
        val login = paths.path("/api/auth/login").path("post")
        assertTrue(login.path("description").asText().contains("refresh_token"))
        val loginResponse = document.at("/components/schemas/LoginResponse/properties")
        assertEquals(setOf("email", "role"), loginResponse.fieldNames().asSequence().toSet())
        assertEquals("binary", paths.path("/api/admin/upload/template").path("get")
            .at("/responses/200/content/application~1octet-stream/schema/format").asText())
    }

    @Test
    fun `login rate limit documentation describes retry delay and problem details`() {
        val result = mockMvc.get("/v3/api-docs").andExpect { status { isOk() } }.andReturn()
        val document = objectMapper.readTree(result.response.contentAsString)
        val login = document.path("paths").path("/api/auth/login").path("post")
        val responseRef = login.at("/responses/429/\$ref").asText()
        assertEquals("#/components/responses/LoginRateLimited", responseRef)

        val response = document.at(responseRef.removePrefix("#"))
        val retryAfter = response.at("/headers/Retry-After/schema")
        assertEquals("integer", retryAfter.path("type").asText())
        assertEquals("int64", retryAfter.path("format").asText())
        assertEquals(1L, retryAfter.path("minimum").asLong())
        assertEquals(30L, retryAfter.path("example").asLong())
        assertEquals("no-store", response.at("/headers/Cache-Control/schema/example").asText())

        val media = response.path("content").path("application/problem+json")
        assertEquals("#/components/schemas/ApiProblem", media.at("/schema/\$ref").asText())
        val example = media.at("/examples/problem/value")
        assertEquals("urn:cardiocenter:problem:login-rate-limit", example.path("type").asText())
        assertEquals("Слишком много попыток входа", example.path("title").asText())
        assertEquals(429, example.path("status").asInt())
        assertEquals("Повторите попытку позже", example.path("detail").asText())
        assertEquals("/api/auth/login", example.path("instance").asText())
        assertEquals("LOGIN_RATE_LIMIT_EXCEEDED", example.path("code").asText())
        assertFalse(example.has("errors"))

        document.path("paths").fields().forEachRemaining { (path, methods) ->
            methods.fields().forEachRemaining { (method, operation) ->
                if (path != "/api/auth/login" || method != "post") {
                    assertFalse(operation.path("responses").has("429"), "$method $path")
                }
            }
        }
    }

    @Test
    fun `login documents unavailable rate limit checks with the shared service unavailable contract`() {
        val result = mockMvc.get("/v3/api-docs").andExpect { status { isOk() } }.andReturn()
        val document = objectMapper.readTree(result.response.contentAsString)
        val responseRef = document.path("paths").path("/api/auth/login").path("post")
            .at("/responses/503/\$ref").asText()
        assertEquals("#/components/responses/ServiceUnavailable", responseRef)

        val response = document.at(responseRef.removePrefix("#"))
        assertEquals("no-store", response.at("/headers/Cache-Control/schema/example").asText())
        assertFalse(response.path("headers").has("Retry-After"))
        val media = response.path("content").path("application/problem+json")
        assertEquals("#/components/schemas/ApiProblem", media.at("/schema/\$ref").asText())
        val example = media.at("/examples/problem/value")
        assertEquals(503, example.path("status").asInt())
        assertEquals("urn:cardiocenter:problem:service-unavailable", example.path("type").asText())
        assertEquals("Сервис временно недоступен", example.path("title").asText())
        assertEquals("Сервис временно не может выполнить запрос.", example.path("detail").asText())
        assertEquals("SERVICE_UNAVAILABLE", example.path("code").asText())
        assertEquals("/api/auth/login", example.path("instance").asText())
        assertFalse(example.has("errors"))

        document.path("paths").fields().forEachRemaining { (path, methods) ->
            methods.fields().forEachRemaining { (method, operation) ->
                if (path != "/api/auth/login" || method != "post") {
                    assertFalse(operation.path("responses").has("503"), "$method $path")
                }
            }
        }
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
