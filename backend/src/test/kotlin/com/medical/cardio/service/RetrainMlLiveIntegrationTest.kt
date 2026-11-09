package com.medical.cardio.service

import com.medical.cardio.exception.MlRetrainRejectedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import com.fasterxml.jackson.databind.JsonNode

@EnabledIfEnvironmentVariable(named = "ML_INTEGRATION_URL", matches = ".+")
class RetrainMlLiveIntegrationTest {
    @Test
    fun `real ML accepts multipart and rejects incomplete CSV before training`() {
        val url = System.getenv("ML_INTEGRATION_URL")
        val client = MlClient(WebClient.builder(), url)
        val healthClient = WebClient.create(url)
        fun version(): String = checkNotNull(healthClient.get().uri("/health")
            .retrieve().bodyToMono(JsonNode::class.java).block()).path("model_version").asText()
        val before = version()
        assertTrue(before.isNotBlank())
        val schema = checkNotNull(client.getDatasetSchema().block())
        assertEquals("patients_wide", schema.path("format").asText())
        assertEquals(69, schema.path("csv_columns").size())
        val error = assertThrows<MlRetrainRejectedException> {
            client.retrain("id,age\nsynthetic,55".toByteArray()).block()
        }
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.status)
        assertEquals(before, version())
    }
}
