package com.medical.cardio.service

import com.medical.cardio.exception.MlServiceException
import org.springframework.stereotype.Service

data class RetrainResult(
    val status: String,
    val savedModels: List<String>,
    val modelVersion: String,
    val inputPatients: Int,
    val preparedRows: Map<String, Int>,
    val modelsStats: List<OneModelStats>
)

data class OneModelStats(
    val modelStem: String,
    val target: String? = null,
    val iterations: Int? = null,
    val threshold: Double? = null,
    val modelVariant: String? = null,
    val trainPatients: Int? = null,
    val validationRows: Int? = null,
    val validationPatients: Int? = null,
    val validationPos: Int? = null,
    val validationNeg: Int? = null,
    val testPatients: Int? = null,
    val modelName: String,
    val featureCount: Int,
    val trainRows: Int,
    val testRows: Int,
    val trainPos: Int,
    val trainNeg: Int,
    val testPos: Int,
    val testNeg: Int,
    val classWeight0: Double,
    val classWeight1: Double,
    val rocAuc: Double,
    val prAuc: Double,
    val accuracy: Double,
    val balancedAccuracy: Double,
    val precision: Double,
    val recall: Double,
    val f1: Double,
    val logloss: Double,
    val brier: Double,
    val tn: Int,
    val fp: Int,
    val fn: Int,
    val tp: Int,
)

data class FullModelsResponse(
    val modelVersion: String,
    val savedModels: List<String>,
    val modelsStats: List<OneModelStats>,
)

@Service
class RetrainService(
    private val mlClient: MlClient,
) {
    fun getModelStats(): FullModelsResponse {
        // Read-only retry: a retrain may publish a new pair while metrics are read.
        repeat(2) {
            val before = activeVersion()
            val metrics = mlClient.getMetrics().block()
                ?: throw MlServiceException(IllegalStateException("Empty ML metrics response"))
            val after = activeVersion()
            if (before == after) {
                if (metrics.size != 2 || metrics.map { it.modelStem }.toSet() != setOf("city", "region")) {
                    throw MlServiceException(IllegalStateException("Incomplete ML metrics pair"))
                }
                return FullModelsResponse(
                    modelVersion = after,
                    savedModels = metrics.map { "${it.modelStem}_model.cbm" },
                    modelsStats = metrics.map { it.toStats() }
                )
            }
        }
        throw MlServiceException(IllegalStateException("ML version changed during metrics read"))
    }

    private fun activeVersion(): String = mlClient.getModelVersion().block()
        ?: throw MlServiceException(IllegalStateException("Empty ML model version"))

    fun getDatasetSchema(): com.fasterxml.jackson.databind.JsonNode =
        mlClient.getDatasetSchema().block()
            ?: throw MlServiceException(IllegalStateException("Empty ML dataset schema"))

    fun retrain(csvBytes: ByteArray): RetrainResult {
        require(csvBytes.isNotEmpty()) { "Dataset is empty" }
        val response = mlClient.retrain(csvBytes).block()
            ?: throw MlServiceException(IllegalStateException("Empty ML retrain response"))
        return RetrainResult(
            status = response.status,
            savedModels = response.savedModels,
            modelVersion = response.modelVersion,
            inputPatients = response.inputPatients,
            preparedRows = response.preparedRows,
            modelsStats = response.metrics.map { it.toStats() }
        )
    }
}

private fun MlMetric.toStats(): OneModelStats = OneModelStats(
    modelStem = modelStem,
    target = target,
    iterations = iterations,
    threshold = threshold,
    modelVariant = modelVariant,
    trainPatients = trainPatients,
    validationRows = validationRows,
    validationPatients = validationPatients,
    validationPos = validationPos,
    validationNeg = validationNeg,
    testPatients = testPatients,
    modelName = MlClient.DISEASE_NAMES[this.modelStem] ?: this.modelStem,
    featureCount = this.featureCount,
    trainRows = this.trainRows,
    testRows = this.testRows,
    trainPos = this.trainPos,
    trainNeg = this.trainNeg,
    testPos = this.testPos,
    testNeg = this.testNeg,
    classWeight0 = this.classWeight0,
    classWeight1 = this.classWeight1,
    rocAuc = this.rocAuc,
    prAuc = this.prAuc,
    accuracy = this.accuracy,
    balancedAccuracy = this.balancedAccuracy,
    precision = this.precision,
    recall = this.recall,
    f1 = this.f1,
    logloss = this.logloss,
    brier = this.brier,
    tn = this.tn,
    fp = this.fp,
    fn = this.fn,
    tp = this.tp,
)
