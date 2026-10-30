package com.medical.cardio.exception

import com.medical.cardio.controller.ApiErrorController
import jakarta.servlet.RequestDispatcher
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartFile

@ExtendWith(OutputCaptureExtension::class)
class MvcProblemDetailsTest {
    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setUp() {
        mvc = MockMvcBuilders.standaloneSetup(ProbeController(), ApiErrorController())
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `malformed JSON is a safe 400 without parser details`() {
        problem(mvc.perform(post("/api/problem-test/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"valid","password":"secret-marker",broken}""")),
            400, "BAD_REQUEST", "/api/problem-test/body")
            .andExpect(content().string(not(containsString("secret-marker"))))
            .andExpect(content().string(not(containsString("JsonParseException"))))
    }

    @Test
    fun `JSON with wrong value type is a safe 400`() {
        problem(mvc.perform(post("/api/problem-test/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"valid","password":"valid","count":"private-invalid-number"}""")),
            400, "BAD_REQUEST", "/api/problem-test/body")
            .andExpect(content().string(not(containsString("private-invalid-number"))))
    }

    @Test
    fun `missing body is a structured 400`() {
        problem(mvc.perform(post("/api/problem-test/body").contentType(MediaType.APPLICATION_JSON)),
            400, "BAD_REQUEST", "/api/problem-test/body")
    }

    @Test
    fun `validation preserves multiple errors per field and never logs rejected passwords`(output: CapturedOutput) {
        val secret = "pw"
        problem(mvc.perform(post("/api/problem-test/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"","password":"$secret"}""")),
            400, "VALIDATION_ERROR", "/api/problem-test/body", validation = true)
            .andExpect(jsonPath("$.errors[?(@.field == 'name')].message", hasSize<Any>(2)))
            .andExpect(jsonPath("$.errors[?(@.field == 'password')].message", hasSize<Any>(1)))
            .andExpect(jsonPath("$.errors[*].rejectedValue").doesNotExist())
        assertFalse(output.all.contains("rejected value"))
        assertFalse(output.all.contains("password=$secret"))
    }

    @Test
    fun `invalid path parameter is a safe 400`() {
        problem(mvc.perform(get("/api/problem-test/path/private-invalid-id")),
            400, "BAD_REQUEST", "/api/problem-test/path/private-invalid-id")
            .andExpect(jsonPath("$.detail", not(containsString("private-invalid-id"))))
    }

    @Test
    fun `missing query parameter is a structured 400`() {
        problem(mvc.perform(get("/api/problem-test/query")), 400, "BAD_REQUEST", "/api/problem-test/query")
    }

    @Test
    fun `invalid query parameter is a structured 400`() {
        problem(mvc.perform(get("/api/problem-test/query").param("count", "wrong")),
            400, "BAD_REQUEST", "/api/problem-test/query")
    }

    @Test
    fun `method parameter constraint is a validation problem`() {
        problem(mvc.perform(get("/api/problem-test/query").param("count", "0")),
            400, "VALIDATION_ERROR", "/api/problem-test/query", validation = true)
            .andExpect(jsonPath("$.errors[0].field").value("count"))
    }

    @Test
    fun `missing header is a structured 400`() {
        problem(mvc.perform(get("/api/problem-test/header")), 400, "BAD_REQUEST", "/api/problem-test/header")
    }

    @Test
    fun `missing multipart file is a structured 400`() {
        problem(mvc.perform(multipart("/api/problem-test/upload")), 400, "BAD_REQUEST", "/api/problem-test/upload")
    }

    @Test
    fun `unsupported HTTP method preserves Allow header`() {
        problem(mvc.perform(get("/api/problem-test/body")), 405, "METHOD_NOT_ALLOWED", "/api/problem-test/body")
            .andExpect(header().string("Allow", containsString("POST")))
    }

    @Test
    fun `unsupported request content type is a structured 415`() {
        problem(mvc.perform(post("/api/problem-test/body").contentType(MediaType.TEXT_PLAIN).content("private-body")),
            415, "UNSUPPORTED_MEDIA_TYPE", "/api/problem-test/body")
    }

    @Test
    fun `unsupported response format is a structured 406`() {
        problem(mvc.perform(get("/api/problem-test/json").accept(MediaType.APPLICATION_XML)),
            406, "NOT_ACCEPTABLE", "/api/problem-test/json")
    }

    @Test
    fun `unknown route is a structured 404`() {
        problem(mvc.perform(get("/api/problem-test/unknown")), 404, "NOT_FOUND", "/api/problem-test/unknown")
    }

    @Test
    fun `oversized upload is a structured 413`() {
        problem(mvc.perform(get("/api/problem-test/too-large")), 413, "PAYLOAD_TOO_LARGE", "/api/problem-test/too-large")
    }

    @Test
    fun `async request timeout preserves 503`() {
        problem(mvc.perform(get("/api/problem-test/timeout")), 503, "SERVICE_UNAVAILABLE", "/api/problem-test/timeout")
    }

    @Test
    fun `unexpected failure is a safe 500 with diagnostics only in logs`(output: CapturedOutput) {
        problem(mvc.perform(get("/api/problem-test/failure")), 500, "INTERNAL_SERVER_ERROR", "/api/problem-test/failure")
            .andExpect(content().string(not(containsString("internal-db"))))
            .andExpect(content().string(not(containsString("IllegalStateException"))))
        org.junit.jupiter.api.Assertions.assertTrue(output.all.contains("internal-db"))
    }

    @Test
    fun `servlet fallback hides exception and preserves original path`() {
        problem(mvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/original")
            .requestAttr(RequestDispatcher.ERROR_MESSAGE, "internal-db")
            .requestAttr(RequestDispatcher.ERROR_EXCEPTION, IllegalStateException("internal-db"))),
            500, "INTERNAL_SERVER_ERROR", "/api/original")
            .andExpect(content().string(not(containsString("internal-db"))))
    }

    @Test
    fun `servlet fallback preserves client error status`() {
        problem(mvc.perform(get("/error")
            .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/absent")),
            404, "NOT_FOUND", "/api/absent")
    }

    private fun problem(result: ResultActions, status: Int, code: String, path: String, validation: Boolean = false): ResultActions {
        result.andExpect(status().`is`(status))
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(status))
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.type", startsWith("urn:cardiocenter:problem:")))
            .andExpect(jsonPath("$.title", not(emptyOrNullString())))
            .andExpect(jsonPath("$.detail", not(emptyOrNullString())))
            .andExpect(jsonPath("$.instance").value(path))
            .andExpect(jsonPath("$.trace").doesNotExist())
            .andExpect(jsonPath("$.exception").doesNotExist())
        if (!validation) result.andExpect(jsonPath("$.errors").doesNotExist())
        return result
    }

    data class ProbeRequest(
        @field:NotBlank @field:Size(min = 2) val name: String,
        @field:Size(min = 12) val password: String,
        val count: Int = 1
    )

    @Profile("mvc-problem-test")
    @RestController
    @RequestMapping("/api/problem-test")
    class ProbeController {
        @PostMapping("/body")
        fun body(@Valid @RequestBody request: ProbeRequest) = mapOf("name" to request.name)

        @GetMapping("/query")
        fun query(@RequestParam("count") @Min(1) count: Int) = count

        @GetMapping("/path/{id}")
        fun path(@PathVariable("id") id: Long) = id

        @GetMapping("/header")
        fun header(@RequestHeader("X-Required") value: String) = value

        @PostMapping("/upload")
        fun upload(@RequestParam("file") file: MultipartFile) = file.size

        @GetMapping("/json", produces = [MediaType.APPLICATION_JSON_VALUE])
        fun json() = mapOf("ok" to true)

        @GetMapping("/too-large")
        fun tooLarge(): Unit = throw MaxUploadSizeExceededException(10L * 1024 * 1024)

        @GetMapping("/timeout")
        fun timeout(): Unit = throw AsyncRequestTimeoutException()

        @GetMapping("/failure")
        fun failure(): Unit = throw IllegalStateException("Cannot connect to internal-db")
    }
}
