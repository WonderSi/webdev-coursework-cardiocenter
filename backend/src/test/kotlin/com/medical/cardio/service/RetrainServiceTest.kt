package com.medical.cardio.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.medical.cardio.exception.MlServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono

class RetrainServiceTest {
    private val client = mock<MlClient>()
    private val service = RetrainService(client)
    private val metrics = jacksonObjectMapper().readValue(
        checkNotNull(javaClass.getResource("/ml/retrain-response.json")).readText(), MlRetrainResponse::class.java
    ).metrics

    @Test
    fun `metrics retry when training publishes between version reads`() {
        whenever(client.getModelVersion()).thenReturn(
            Mono.just("old"), Mono.just("new"), Mono.just("new"), Mono.just("new")
        )
        whenever(client.getMetrics()).thenReturn(
            Mono.just(metrics), Mono.just(metrics.map { it.copy(accuracy = 0.81) })
        )
        val response = service.getModelStats()
        assertEquals("new", response.modelVersion)
        assertEquals(0.81, response.modelsStats.first().accuracy)
        verify(client, times(2)).getMetrics()
    }

    @Test
    fun `continual version changes fail instead of reporting mixed metadata`() {
        whenever(client.getModelVersion()).thenReturn(
            Mono.just("a"), Mono.just("b"), Mono.just("c"), Mono.just("d")
        )
        whenever(client.getMetrics()).thenReturn(Mono.just(metrics))
        assertThrows<MlServiceException> { service.getModelStats() }
        verify(client, times(2)).getMetrics()
    }

    @Test
    fun `missing health version and incomplete metrics are rejected`() {
        whenever(client.getModelVersion()).thenReturn(Mono.empty())
        assertThrows<MlServiceException> { service.getModelStats() }
        whenever(client.getModelVersion()).thenReturn(Mono.just("active"))
        whenever(client.getMetrics()).thenReturn(Mono.just(metrics.take(1)))
        assertThrows<MlServiceException> { service.getModelStats() }
    }
}
