package com.medical.cardio.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

data class DashboardCount(val name: String, val count: Long)
data class DashboardAgeCount(val ageGroup: String, val gender: String, val count: Long)

@Repository
class DashboardRepository(private val jdbc: JdbcTemplate) {
    fun summary(modelKind: String): Map<String, Any?> = jdbc.queryForMap("""
        SELECT count(*) AS total, avg(age) AS average_age,
               count(*) FILTER (WHERE EXISTS (SELECT 1 FROM diagnoses d WHERE d.patient_id = p.id)) AS diagnosed
        FROM patients p WHERE model_kind = ?
    """.trimIndent(), modelKind)

    fun byGender(modelKind: String): List<DashboardCount> = byGlossary(modelKind, "gender_id")
    fun byAlcohol(modelKind: String): List<DashboardCount> = byGlossary(modelKind, "alcohol_id")
    fun byProfession(modelKind: String): List<DashboardCount> = byGlossary(modelKind, "profession_id")
    fun byDistrict(modelKind: String): List<DashboardCount> = byGlossary(modelKind, "region_id")

    private fun byGlossary(modelKind: String, column: String): List<DashboardCount> {

        require(column in setOf("gender_id", "alcohol_id", "profession_id", "region_id"))
        return counts("""
            SELECT coalesce(g.value, 'Нет данных') AS name, count(*) AS count
            FROM patients p LEFT JOIN glossary_values g ON g.id = p.$column
            WHERE p.model_kind = ? GROUP BY g.value ORDER BY count DESC, name
        """.trimIndent(), modelKind)
    }

    fun byAgeAndGender(modelKind: String): List<DashboardAgeCount> = jdbc.query("""
        SELECT CASE WHEN age < 30 THEN '<30' WHEN age < 40 THEN '30–39'
                    WHEN age < 50 THEN '40–49' WHEN age < 60 THEN '50–59'
                    WHEN age < 70 THEN '60–69' ELSE '70+' END AS age_group,
               g.value AS gender, count(*) AS count
        FROM patients p JOIN glossary_values g ON g.id = p.gender_id
        WHERE model_kind = ? GROUP BY age_group, g.value
    """.trimIndent(), { row, _ ->
        DashboardAgeCount(row.getString("age_group"), row.getString("gender"), row.getLong("count"))
    }, modelKind)

    fun byBmi(modelKind: String): List<DashboardCount> = counts("""
        SELECT CASE WHEN height IS NULL OR height <= 0 OR weight IS NULL OR weight <= 0 THEN 'Нет данных'
                    WHEN weight / power(height / 100.0, 2) < 18.5 THEN 'Недостаточный'
                    WHEN weight / power(height / 100.0, 2) < 25 THEN 'Нормальный'
                    WHEN weight / power(height / 100.0, 2) < 30 THEN 'Избыточный'
                    ELSE 'Ожирение' END AS name, count(*) AS count
        FROM patients WHERE model_kind = ? GROUP BY name
    """.trimIndent(), modelKind)

    fun byLungDisease(modelKind: String): List<DashboardCount> = counts("""
        SELECT CASE chronic_lung_disease WHEN 1 THEN 'Да' WHEN 0 THEN 'Нет' ELSE 'Нет данных' END AS name,
               count(*) AS count FROM patients WHERE model_kind = ? GROUP BY name
    """.trimIndent(), modelKind)

    fun labCoverage(modelKind: String): Map<String, Any?> = jdbc.queryForMap("""
        SELECT count(glucose) AS glucose, count(cholesterol) AS cholesterol,
               count(hdl_cholesterol) AS hdl_cholesterol, count(ldl_cholesterol) AS ldl_cholesterol,
               count(triglycerides) AS triglycerides FROM patients WHERE model_kind = ?
    """.trimIndent(), modelKind)

    fun recordedDiagnoses(modelKind: String): List<DashboardCount> = counts("""
        SELECT g.value AS name, count(DISTINCT p.id) AS count
        FROM diagnoses d JOIN patients p ON p.id = d.patient_id
        JOIN glossary_values g ON g.id = d.diagnosis_id
        WHERE p.model_kind = ? GROUP BY g.value ORDER BY count DESC, name
    """.trimIndent(), modelKind)

    private fun counts(sql: String, modelKind: String): List<DashboardCount> =
        jdbc.query(sql, { row, _ -> DashboardCount(row.getString("name"), row.getLong("count")) }, modelKind)
}
