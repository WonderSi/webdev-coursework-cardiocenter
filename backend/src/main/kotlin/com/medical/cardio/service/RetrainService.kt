package com.medical.cardio.service

import com.medical.cardio.entity.PatientEntity
import com.medical.cardio.repository.PatientRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class RetrainResult(
    val status: String,
    val savedModels: List<String>
)

data class OneModelStats(
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
    private val patientRepository: PatientRepository,
    private val mlClient: MlClient,
) {
    fun getModelStats(): FullModelsResponse {
        val metrics = mlClient.getMetrics().block()
            ?: throw RuntimeException("Нет ответа от ML-сервиса")
        return FullModelsResponse(
            modelVersion = "1.0.0",
            savedModels = metrics.map { "${it.modelStem}_model.cbm" },
            modelsStats = metrics.map { m ->
                OneModelStats(
                    modelName = MlClient.DISEASE_NAMES[m.modelStem] ?: m.modelStem,
                    featureCount = m.featureCount,
                    trainRows = m.trainRows,
                    testRows = m.testRows,
                    trainPos = m.trainPos,
                    trainNeg = m.trainNeg,
                    testPos = m.testPos,
                    testNeg = m.testNeg,
                    classWeight0 = m.classWeight0,
                    classWeight1 = m.classWeight1,
                    rocAuc = m.rocAuc,
                    prAuc = m.prAuc,
                    accuracy = m.accuracy,
                    balancedAccuracy = m.balancedAccuracy,
                    precision = m.precision,
                    recall = m.recall,
                    f1 = m.f1,
                    logloss = m.logloss,
                    brier = m.brier,
                    tn = m.tn,
                    fp = m.fp,
                    fn = m.fn,
                    tp = m.tp,
                )
            }
        )
    }

    @Transactional(readOnly = true)
    fun retrain(): RetrainResult {
        val patients = patientRepository.findAllWithDiagnoses()
        val csvBytes = generateCsv(patients)
        val response = mlClient.retrain(csvBytes).block()
            ?: throw RuntimeException("Нет ответа от ML-сервиса")
        return RetrainResult(status = response.status, savedModels = response.savedModels)
    }

    private fun generateCsv(patients: List<PatientEntity>): ByteArray {
        val sb = StringBuilder()
        sb.append("id,year,пол,алкоголь,профессия,район,возраст,рост,вес,")
        sb.append("холестерин,холестерин лпвп,холестерин лпнп,тг,об,глюкоза,")
        sb.append("не лпвп,лпонп,липиды apoa,липиды apo b,имт,")
        sb.append("инсульт,сн,нарушение ритма или ибс,стенокардия,им,аг,")
        sb.appendLine("инсульт_год,сн_год,нарушение_ритма_год,стенокардия_год,им_год,аг_год")

        for (patient in patients) {
            val regionStr = REGION_MAP[patient.region?.code] ?: continue

            val diagCodes = patient.diagnoses.map { it.diagnosis.code }.toSet()
            val yearMap   = patient.diagnoses
                .filter { it.yearOfDiagnosis != null }
                .associate { it.diagnosis.code to it.yearOfDiagnosis!! }

            val h = patient.height?.toDouble()
            val w = patient.weight?.toDouble()
            val bmi = if (h != null && w != null && h > 0) {
                val hM = h / 100.0
                (w / (hM * hM)).toString()
            } else ""

            val row = listOf(
                patient.id.toString(),
                patient.createDate.year.toString(),
                GENDER_MAP[patient.gender.code] ?: "",
                ALCOHOL_MAP[patient.alcohol?.code] ?: "",
                csvEsc(PROFESSION_MAP[patient.profession?.code] ?: ""),
                regionStr,
                patient.age.toString(),
                patient.height?.toPlainString() ?: "",
                patient.weight?.toPlainString() ?: "",
                patient.cholesterol?.toPlainString() ?: "",
                patient.hdlCholesterol?.toPlainString() ?: "",
                patient.ldlCholesterol?.toPlainString() ?: "",
                patient.triglycerides?.toPlainString() ?: "",
                patient.hipMeasurement?.toPlainString() ?: "",
                patient.glucose?.toPlainString() ?: "",
                patient.nonHdlCholesterol?.toPlainString() ?: "",
                patient.vldlCholesterol?.toPlainString() ?: "",
                patient.apolipoproteinA?.toPlainString() ?: "",
                patient.apolipoproteinB?.toPlainString() ?: "",
                bmi,
                if (1 in diagCodes) "1" else "0",
                if (2 in diagCodes) "1" else "0",
                if (3 in diagCodes) "1" else "0",
                if (4 in diagCodes) "1" else "0",
                if (5 in diagCodes) "1" else "0",
                if (6 in diagCodes) "1" else "0",
                yearMap[1]?.toString() ?: "",
                yearMap[2]?.toString() ?: "",
                yearMap[3]?.toString() ?: "",
                yearMap[4]?.toString() ?: "",
                yearMap[5]?.toString() ?: "",
                yearMap[6]?.toString() ?: "",
            )
            sb.appendLine(row.joinToString(","))
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun csvEsc(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n'))
            "\"${value.replace("\"", "\"\"")}\""
        else value

    companion object {
        private val GENDER_MAP = mapOf(1 to "муж", 2 to "жен")
        private val ALCOHOL_MAP = mapOf(
            1 to "нет",
            2 to "ранее употреблял",
            3 to "да в настоящее время",
        )
        private val PROFESSION_MAP = mapOf(
            1  to "ведение домашнего хозяйства",
            2  to "вооруженные силы",
            3  to "лица свободных профессий",
            4  to "низкоквалифицированные  и неквалифицированные работники, рабочие, ручной труд",
            5  to "операторы и монтажники установок и машинного оборудования",
            6  to "служащие, сфера обслуживания, работники среднего звена",
            7  to "никогда не работающие домохозяйки",
            8  to "дипломированные специалисты, умственный труд",
            9  to "другое",
            10 to "квалифицированные специалисты сельского хозяйства и рыболовного",
            11 to "пенсионеры",
            12 to "ремесленники и представители других отраслей промышленности",
            13 to "техники и младшие специалисты",
            14 to "представители законодательных органов власти, высокопоставленные должностные лица, менеджеры и руководители",
        )
        private val REGION_MAP = mapOf(
            1 to "рудничный",
            2 to "центральный",
            3 to "заводский",
            4 to "кировский",
            5 to "ленинский",
            6 to "сельская местность",
        )
    }
}
