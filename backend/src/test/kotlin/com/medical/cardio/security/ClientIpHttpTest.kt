package com.medical.cardio.security

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

// Start only the HTTP server and MVC: these tests need neither a database nor Redis.
// The dedicated profile prevents this configuration from entering other test contexts.
@Configuration(proxyBeanMethods = false)
@Profile("client-ip-http-test")
@ImportAutoConfiguration(
    ServletWebServerFactoryAutoConfiguration::class,
    EmbeddedWebServerFactoryCustomizerAutoConfiguration::class,
    DispatcherServletAutoConfiguration::class,
    HttpMessageConvertersAutoConfiguration::class,
    WebMvcAutoConfiguration::class,
)
@Import(ClientIpProbeController::class)
class ClientIpTestApplication

@RestController
@Profile("client-ip-http-test")
class ClientIpProbeController {
    @GetMapping("/test/client-ip", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun clientIp(request: HttpServletRequest): String = request.remoteAddr
}

@SpringBootTest(
    classes = [ClientIpTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("client-ip-http-test")
abstract class ClientIpHttpTest {
    @LocalServerPort
    private var port: Int = 0

    protected fun clientIp(vararg headers: Pair<String, String>): String {
        val requestBuilder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:$port/test/client-ip"))
            .timeout(Duration.ofSeconds(5))
            .GET()
        headers.forEach { (name, value) -> requestBuilder.header(name, value) }

        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build().use { client ->
            val response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
            assertEquals(200, response.statusCode())
            response.body()
        }
    }
}

class UntrustedProxyClientIpHttpTest : ClientIpHttpTest() {
    @Test
    fun `direct requests keep the connection address`() {
        assertEquals("127.0.0.1", clientIp())
    }

    @ParameterizedTest
    @CsvSource(
        "X-Real-IP,203.0.113.10",
        "X-Forwarded-For,203.0.113.10",
        "Forwarded,for=203.0.113.10",
    )
    fun `untrusted clients cannot replace their address with headers`(name: String, value: String) {
        assertEquals("127.0.0.1", clientIp(name to value))
    }
}

@TestPropertySource(properties = ["server.tomcat.remoteip.internal-proxies=127\\.0\\.0\\.1"])
class TrustedProxyClientIpHttpTest : ClientIpHttpTest() {
    @ParameterizedTest
    @CsvSource("203.0.113.10", "2001:db8::10")
    fun `trusted proxy supplies the client address through the configured header`(address: String) {
        assertEquals(address, clientIp("X-Real-IP" to address))
    }

    @Test
    fun `trusted proxy without a client address keeps the connection address`() {
        assertEquals("127.0.0.1", clientIp())
    }

    @Test
    fun `forwarded headers cannot override the configured real IP header`() {
        assertEquals("203.0.113.10", clientIp(
            "X-Real-IP" to "203.0.113.10",
            "X-Forwarded-For" to "198.51.100.20",
            "Forwarded" to "for=198.51.100.20",
        ))
    }

    @Test
    fun `forwarded headers alone do not supply the client address`() {
        assertEquals("127.0.0.1", clientIp(
            "X-Forwarded-For" to "198.51.100.20",
            "Forwarded" to "for=198.51.100.20",
        ))
    }
}
