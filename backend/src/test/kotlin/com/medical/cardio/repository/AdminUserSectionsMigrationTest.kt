package com.medical.cardio.repository

import com.medical.cardio.BaseIntegrationTest
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID
import javax.sql.DataSource
import kotlin.test.assertEquals

class AdminUserSectionsMigrationTest @Autowired constructor(
    private val dataSource: DataSource
) : BaseIntegrationTest() {

    @Test
    fun `V9 grants all current sections only to existing doctors`() {
        val schema = "sections_migration_" + UUID.randomUUID().toString().replace("-", "")
        val jdbc = JdbcTemplate(dataSource)
        try {
            migrate(schema, "8")
            jdbc.update("""
                INSERT INTO $schema.admin_users (email, password_hash, role)
                VALUES ('existing-doctor@cardio.test', 'unused-test-hash', 'DOCTOR'),
                       ('existing-admin@cardio.test', 'unused-test-hash', 'SUPER_ADMIN')
            """.trimIndent())

            migrate(schema, "9")

            val sections = jdbc.queryForList("""
                SELECT s.section FROM $schema.admin_user_sections s
                JOIN $schema.admin_users u ON u.id = s.admin_user_id
                WHERE u.email = 'existing-doctor@cardio.test'
            """.trimIndent(), String::class.java).toSet()
            assertEquals(setOf("DASHBOARDS", "UPLOAD_DATA", "MODEL_TRAINING", "VIEW_DATA"), sections)
            assertEquals(4L, jdbc.queryForObject(
                "SELECT count(*) FROM $schema.admin_user_sections", Long::class.java))

            jdbc.update("""
                INSERT INTO $schema.admin_users (email, password_hash, role)
                VALUES ('new-doctor@cardio.test', 'unused-test-hash', 'DOCTOR')
            """.trimIndent())
            assertEquals(4L, jdbc.queryForObject(
                "SELECT count(*) FROM $schema.admin_user_sections", Long::class.java))
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS $schema CASCADE")
        }
    }

    private fun migrate(schema: String, version: String) {
        Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .defaultSchema(schema)
            .locations("classpath:db/migration")
            .target(version)
            .load()
            .migrate()
    }
}
