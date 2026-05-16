package com.medical.cardio.service.upload

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.GlossaryValueEntity
import com.medical.cardio.entity.PatientEntity
import java.time.LocalDate

data class DiagnosisImportEntry(
    val diagnosis: GlossaryValueEntity,
    val year: Short?
)

data class PatientImportData(
    val patient: PatientEntity,
    val diagnosisEntries: List<DiagnosisImportEntry>
)

object PatientMapper {

    // (excelFlagColumn, excelYearColumn, diagnosisGlossaryCode)
    private val DIAGNOSIS_COLUMNS = listOf(
        Triple("stroke",                "stroke_year",           1),
        Triple("heart_failure",         "heart_failure_year",    2),
        Triple("cad",                   "cad_year",              3),
        Triple("angina",                "angina_year",           4),
        Triple("myocardial_infarction", "mi_year",               5),
        Triple("arterial_hypertension", "ah_year",               6),
    )

    fun map(row: Map<String, String>, resolver: GlossaryResolver, creator: AdminUserEntity): PatientImportData {
        val gender = resolver.resolve("gender", row["gender"]!!.toInt())
            ?: throw IllegalArgumentException("Код пола не найден: ${row["gender"]}")

        val patient = PatientEntity(
            createDate       = LocalDate.now(),
            creator          = creator,
            gender           = gender,
            age              = row["age"]!!.toInt(),
            height           = row["height"]?.trim()?.toBigDecimalOrNull(),
            weight           = row["weight"]?.trim()?.toBigDecimalOrNull(),
            hipMeasurement   = row["hip_measurement"]?.trim()?.toBigDecimalOrNull(),
            alcohol          = row["alcohol"]?.trim()?.toIntOrNull()?.let { resolver.resolve("alcohol", it) },
            profession       = row["profession"]?.trim()?.toIntOrNull()?.let { resolver.resolve("profession", it) },
            region           = row["region"]?.trim()?.toIntOrNull()?.let { resolver.resolve("region", it) },
            glucose          = row["glucose"]?.trim()?.toBigDecimalOrNull(),
            cholesterol      = row["cholesterol"]?.trim()?.toBigDecimalOrNull(),
            nonHdlCholesterol= row["non_hdl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            vldlCholesterol  = row["vldl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            hdlCholesterol   = row["hdl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            ldlCholesterol   = row["ldl_cholesterol"]?.trim()?.toBigDecimalOrNull(),
            apolipoproteinA  = row["apolipoprotein_a"]?.trim()?.toBigDecimalOrNull(),
            apolipoproteinB  = row["apolipoprotein_b"]?.trim()?.toBigDecimalOrNull(),
            triglycerides    = row["triglycerides"]?.trim()?.toBigDecimalOrNull(),
        )

        val diagnoses = DIAGNOSIS_COLUMNS.mapNotNull { (flagCol, yearCol, code) ->
            if (row[flagCol]?.trim() == "1") {
                val glossaryValue = resolver.resolve("diagnosis", code) ?: return@mapNotNull null
                val year = row[yearCol]?.trim()?.toIntOrNull()?.toShort()
                DiagnosisImportEntry(diagnosis = glossaryValue, year = year)
            } else null
        }

        return PatientImportData(patient = patient, diagnosisEntries = diagnoses)
    }
}
