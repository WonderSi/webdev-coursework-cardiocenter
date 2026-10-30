package com.medical.cardio.service

import com.medical.cardio.dto.SurveyRequest
import com.medical.cardio.exception.MlServiceException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.net.ConnectException
import kotlin.test.assertSame
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.TimeoutException

class MlClientTest {
    @Test
    fun `getMetrics rejects empty HTTP body`() {
        assertThrows<MlServiceException> { client(HttpStatus.OK).getMetrics().block() }
    }

    @Test
    fun `getMetrics accepts a valid empty JSON array`() {
        assertEquals(emptyList<MlMetric>(), client(HttpStatus.OK, "[]").getMetrics().block())
    }

    @Test
    fun `all ML operations reject malformed response JSON`() {
        val client = client(HttpStatus.OK, "{invalid-json")
        assertThrows<MlServiceException> { client.predict(request()).block() }
        assertThrows<MlServiceException> { client.retrain("id,age\n1,45".toByteArray()).block() }
        assertThrows<MlServiceException> { client.getMetrics().block() }
    }

    @Test
    fun `predict rejects a response with no predictions`() {
        val client = client(HttpStatus.OK, """{"predictions":[],"model_version":"1"}""")
        assertThrows<MlServiceException> { client.predict(request()).block() }
    }

    @Test
    fun `successful prediction still converts probabilities and orders diseases`() {
        val client = client(HttpStatus.OK, """{"predictions":[{"disease":"stroke","probability":0.2},{"disease":"general","probability":0.4},{"disease":"mi","probability":0.7}],"model_version":"1"}""")
        val result = client.predict(request()).block()!!
        assertEquals(listOf("Общий риск", "Инфаркт миокарда", "Инсульт"), result.map { it.name })
        assertEquals(listOf(40, 70, 20), result.map { it.probability })
    }

    @Test
    fun `successful retrain still decodes model names`() {
        val result = client(HttpStatus.OK, """{"status":"ok","saved_models":["general_model.cbm"]}""")
            .retrain("id,age\n1,45".toByteArray()).block()!!
        assertEquals("ok", result.status)
        assertEquals(listOf("general_model.cbm"), result.savedModels)
    }

    @Test
    fun `ML HTTP 404 remains an upstream failure instead of an API not found`() {
        val exception = assertThrows<MlServiceException> {
            client(HttpStatus.NOT_FOUND, "internal model path").getMetrics().block()
        }
        val cause = assertInstanceOf(WebClientResponseException::class.java, exception.cause)
        assertEquals(404, cause.statusCode.value())
    }

    @Test
    fun `predict and retrain preserve connection failure as cause`() {
        val cause = ConnectException("Connection refused")
        val client = MlClient(WebClient.builder().exchangeFunction { Mono.error(cause) }, "http://ml-test")
        assertSame(cause, assertThrows<MlServiceException> { client.predict(request()).block() }.cause)
        assertSame(cause, assertThrows<MlServiceException> { client.retrain(byteArrayOf()).block() }.cause)
    }

    @Test
    fun `timeout is converted to an ML service failure`() {
        val cause = TimeoutException("ML read timeout")
        val client = MlClient(WebClient.builder().exchangeFunction { Mono.error(cause) }, "http://ml-test")
        assertSame(cause, assertThrows<MlServiceException> { client.getMetrics().block() }.cause)
    }

    private fun client(status: HttpStatus, body: String? = null): MlClient {
        val builder = WebClient.builder().exchangeFunction {
            val response = ClientResponse.create(status).header("Content-Type", "application/json")
            if (body != null) response.body(body)
            Mono.just(response.build())
        }
        return MlClient(builder, "http://ml-test")
    }

    private fun request() = SurveyRequest(
        genderId = 1L, age = 45, height = BigDecimal("175"), weight = BigDecimal("80"),
        alcoholId = 1L, professionId = 1L, regionId = 1L
    )

    @Test
    fun `getMetrics wraps ML server error in MlServiceException`() {
        val builder = WebClient.builder().exchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body ("Internal ML failure")
                    .build()
            )
        }
        val mlClient = MlClient(builder, "http://ml-test")

        val exception = assertThrows<MlServiceException> {
            mlClient.getMetrics().block()
        }

        assertInstanceOf(WebClientResponseException::class.java, exception.cause)
    }

    @Test
    fun `getMetrics preserves connection failure as cause`() {
        val cause = ConnectException("Connection refused")
        val builder = WebClient.builder().exchangeFunction {
            Mono.error(cause)
        }

        val mlClient = MlClient(builder, "http://ml-test")

        val exception = assertThrows<MlServiceException> {
            mlClient.getMetrics().block()
        }

        assertSame(cause, exception.cause)
    }

    @Test
    fun `predict wraps ML server error in MlServiceException`() {
        val builder = WebClient.builder().exchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body ("Internal ML failure")
                    .build()
            )
        }
        val mlClient = MlClient(builder, "http://ml-test")

        val request = SurveyRequest(
            genderId = 1L,
            age = 45,
            height = BigDecimal("175"),
            weight = BigDecimal("80"),
            alcoholId = 1L,
            professionId = 1L,
            regionId = 1L
        )

        val exception = assertThrows<MlServiceException> {
            mlClient.predict(request).block()
        }

        assertInstanceOf(WebClientResponseException::class.java, exception.cause)
    }

    @Test
    fun `predict rejects empty ML response`() {
        val builder = WebClient.builder().exchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.OK).build()
            )
        }
        val mlClient = MlClient(builder, "http://ml-test")

        val request = SurveyRequest(
            genderId = 1L,
            age = 45,
            height = BigDecimal("175"),
            weight = BigDecimal("80"),
            alcoholId = 1L,
            professionId = 1L,
            regionId = 1L
        )

        assertThrows<MlServiceException> {
            mlClient.predict(request).block()
        }
    }

    @Test
    fun `retrain wraps ML server error in MlServiceException`() {
        val builder = WebClient.builder().exchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body ("Internal ML failure")
                    .build()
            )
        }
        val mlClient = MlClient(builder, "http://ml-test")

        val exception = assertThrows<MlServiceException> {
            mlClient.retrain("id,age\n1,45".toByteArray()).block()
        }

        assertInstanceOf(WebClientResponseException::class.java, exception.cause)
    }

    @Test
    fun `retrain rejects empty ML response`() {
        val builder = WebClient.builder().exchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.OK).build()
            )
        }
        val mlClient = MlClient(builder, "http://ml-test")

        assertThrows<MlServiceException> {
            mlClient.retrain("id,age\n1,45".toByteArray()).block()
        }
    }
}
