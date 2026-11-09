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
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.mock.http.client.reactive.MockClientHttpRequest
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.ExchangeStrategies
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertTrue

class MlClientTest {
    @Test
    fun `active model version is read from health and unavailable bundles fail`() {
        assertEquals("active-bundle", client(HttpStatus.OK,
            """{"status":"ok","models":["city","region"],"model_version":"active-bundle"}""")
            .getModelVersion().block())
        for (body in listOf(
            """{"status":"models_not_trained","models":[]}""",
            """{"status":"ok","models":["city"],"model_version":"v"}""",
            """{"status":"ok","models":["city","region"],"model_version":""}"""
        )) assertThrows<MlServiceException> { client(HttpStatus.OK, body).getModelVersion().block() }
    }
    @Test
    fun `retrain rejection preserves expected status without upstream details`() {
        for (status in listOf(HttpStatus.CONFLICT, HttpStatus.PAYLOAD_TOO_LARGE, HttpStatus.UNPROCESSABLE_ENTITY)) {
            val error = assertThrows<com.medical.cardio.exception.MlRetrainRejectedException> {
                client(status, """{"detail":"private clinical value"}""")
                    .retrain("id,history\n1,0".toByteArray()).block()
            }
            assertEquals(status, error.status)
            assertEquals(null, error.cause)
        }
    }

    @Test
    fun `retrain transmits CSV bytes in multipart dataset with a filename`() {
        val csv = "id,history\r\nsynthetic,0\r\n"
        var outgoing = ""
        val client = MlClient(WebClient.builder().exchangeFunction { request ->
            assertEquals("/retrain", request.url().path)
            val output = MockClientHttpRequest(HttpMethod.POST, request.url())
            request.writeTo(output, ExchangeStrategies.withDefaults())
                .then(Mono.defer { output.bodyAsString })
                .doOnNext { outgoing = it }
                .thenReturn(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText())
                    .build())
        }, "http://ml-test")
        client.retrain(csv.toByteArray()).block()
        assertTrue(outgoing.contains("name=\"dataset\""))
        assertTrue(outgoing.contains("filename=\"patients.csv\""))
        assertTrue(outgoing.contains(csv))
    }

    @Test
    fun `retrain rejects successful HTTP response with incomplete model pair`() {
        val mapper = jacksonObjectMapper()
        val response = mapper.readTree(checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText())
        (response.path("saved_models") as com.fasterxml.jackson.databind.node.ArrayNode).remove(1)
        assertThrows<MlServiceException> {
            client(HttpStatus.OK, response.toString()).retrain("id\n1".toByteArray()).block()
        }
    }

    @Test
    fun `outgoing JSON obeys the ML branch schema and excludes removed predictors`() {
        val mapper = jacksonObjectMapper()
        val schema = mapper.readTree(Path.of("../ml/app/data/dataset_schema.json").toFile())
            .path("predict_schema")
        val allowed = schema.path("properties").fieldNames().asSequence().toSet()
        val required = schema.path("required").map { it.asText() }.toSet()
        for ((kind, year) in listOf("city" to 2017, "region" to null)) {
            var outgoing = ""
            val client = MlClient(WebClient.builder().exchangeFunction { request ->
                val output = MockClientHttpRequest(HttpMethod.POST, request.url())
                request.writeTo(output, ExchangeStrategies.withDefaults())
                    .then(Mono.defer { output.bodyAsString })
                    .doOnNext { outgoing = it }
                    .thenReturn(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("""{"predictions":[{"disease":"$kind","probability":0.6,"prediction":1}],"model_version":"bundle-1"}""")
                        .build())
            }, "http://ml-test")
            client.predict(request().copy(
                modelKind = kind, year = year, chronicLungDisease = 1, alcoholId = 3,
                professionId = 13, hipMeasurement = BigDecimal("95"),
                apolipoproteinA = BigDecimal("1.4"), nonHdlCholesterol = BigDecimal("3.5")
            )).block()
            val json = mapper.readTree(outgoing)
            val fields = json.fieldNames().asSequence().toSet()
            assertTrue(allowed.containsAll(fields), outgoing)
            assertTrue(required.all { json.hasNonNull(it) }, outgoing)
            assertEquals(kind, json.path("model_kind").asText())
            assertEquals(2, json.path("alcohol").asInt())
            assertEquals(13, json.path("profession").asInt())
            assertEquals(1, json.path("chronic_lung_disease").asInt())
            if (year == null) assertTrue(json.path("year").isNull)
            else assertEquals(year, json.path("year").asInt())
            assertTrue(json.path("stroke_year").isNull)
        }
    }

    @Test
    fun `prediction rejects model mismatch and invalid probability`() {
        for (response in listOf(
            """{"predictions":[{"disease":"city","probability":0.6,"prediction":1}],"model_version":"1"}""",
            """{"predictions":[{"disease":"region","probability":1.5,"prediction":1}],"model_version":"1"}"""
        )) assertThrows<MlServiceException> { client(HttpStatus.OK, response).predict(request()).block() }
    }

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
    fun `successful prediction converts probability and preserves model metadata`() {
        val client = client(HttpStatus.OK, """{"predictions":[{"disease":"region","probability":0.72,"prediction":1}],"model_version":"bundle-1"}""")
        val result = client.predict(request()).block()!!
        assertEquals("bundle-1", result.modelVersion)
        assertEquals("region", result.predictions.single().modelKind)
        assertEquals(72, result.predictions.single().probability)
        assertEquals(1, result.predictions.single().prediction)
    }

    @Test
    fun `successful retrain still decodes model names`() {
        val result = client(HttpStatus.OK, checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText())
            .retrain("id,age\n1,45".toByteArray()).block()!!
        assertEquals("ok", result.status)
        assertEquals(listOf("city_model.cbm", "region_model.cbm"), result.savedModels)
        assertEquals("test-bundle", result.modelVersion)
        assertEquals(2, result.metrics.size)
        assertEquals(2987, result.inputPatients)
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
        modelKind = "region", chronicLungDisease = 0,
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
