package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID
import javax.sql.DataSource

class PatientImportMigrationTest : BaseIntegrationTest() {
    @Autowired private lateinit var dataSource: DataSource

    @Test
    fun `migration stops before replacing referenced profession codes or changing patient schema`() {
        val schema = "patient_upgrade_" + UUID.randomUUID().toString().replace("-", "")
        val jdbc = JdbcTemplate(dataSource)
        try {
            Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema)
                .target("7").load().migrate()
            jdbc.update("INSERT INTO $schema.admin_users (email, password_hash, role) VALUES (?, ?, ?)",
                "migration@example.invalid", "synthetic-unused-hash", "DOCTOR")
            jdbc.update("""
                INSERT INTO $schema.patients (creator_id, gender_id, age, profession_id)
                SELECT u.id, gender.id, 45, profession.id
                FROM $schema.admin_users u
                JOIN $schema.glossary_values gender ON gender.code = 1
                JOIN $schema.glossaries gender_glossary ON gender_glossary.id = gender.glossary_id AND gender_glossary.name = 'gender'
                JOIN $schema.glossary_values profession ON profession.code = 1
                JOIN $schema.glossaries profession_glossary ON profession_glossary.id = profession.glossary_id AND profession_glossary.name = 'profession'
            """.trimIndent())

            assertThrows(FlywayException::class.java) {
                Flyway.configure().dataSource(dataSource).schemas(schema).defaultSchema(schema).load().migrate()
            }
            assertEquals(1L, jdbc.queryForObject("SELECT COUNT(*) FROM $schema.patients", Long::class.java))
            assertEquals("Ведение домашнего хозяйства", jdbc.queryForObject("""
                SELECT v.value FROM $schema.patients p JOIN $schema.glossary_values v ON v.id = p.profession_id
            """.trimIndent(), String::class.java))
            assertEquals(14L, jdbc.queryForObject("""
                SELECT COUNT(*) FROM $schema.glossary_values v JOIN $schema.glossaries g ON g.id = v.glossary_id
                WHERE g.name = 'profession'
            """.trimIndent(), Long::class.java))
            assertEquals(0L, jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = ?
                AND table_name = 'patients' AND column_name = 'model_kind'
            """.trimIndent(), Long::class.java, schema))
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS $schema CASCADE")
        }
    }
}
