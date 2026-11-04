package com.medical.cardio.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.medical.cardio.BaseIntegrationTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CsrfCookieHttpTest : BaseIntegrationTest() {
    @LocalServerPort private var port: Int = 0
    @Autowired private lateinit var objectMapper: ObjectMapper

    @Test
    fun `csrf cookie carries required attributes over real HTTP`() {
        val request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/api/auth/csrf"))
            .GET().build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        val cookies = response.headers().allValues("set-cookie")
        val csrfCookie = cookies.single { it.startsWith("XSRF-TOKEN=") }
        assertTrue(csrfCookie.contains("Path=/api/auth"))
        assertTrue(csrfCookie.contains("HttpOnly"))
        assertTrue(csrfCookie.contains("SameSite=Strict"))
        assertTrue(cookies.none { it.startsWith("JSESSIONID=") })
        assertTrue(response.headers().firstValue("cache-control").orElse("").contains("no-store"))
        val body = objectMapper.readTree(response.body())
        assertEquals("X-XSRF-TOKEN", body.path("headerName").asText())
        assertTrue(body.path("token").asText().isNotBlank())
    }
}
