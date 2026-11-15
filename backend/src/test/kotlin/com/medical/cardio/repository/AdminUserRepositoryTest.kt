package com.medical.cardio.repository

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminSection
import com.medical.cardio.entity.AdminUserEntity
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Transactional
class AdminUserRepositoryTest @Autowired constructor(
    private val users: AdminUserRepository,
    private val entityManager: EntityManager,
    private val jdbc: JdbcTemplate
) : BaseIntegrationTest() {

    @Test
    fun `saves and loads allowed sections`() {
        val expected = setOf(AdminSection.DASHBOARDS, AdminSection.UPLOAD_DATA)
        val user = users.saveAndFlush(newUser(expected))
        entityManager.clear()

        assertEquals(expected, users.findById(user.id).orElseThrow().allowedSections)
    }

    @Test
    fun `new doctor has no sections by default`() {
        val user = users.saveAndFlush(newUser())
        entityManager.clear()

        assertEquals(emptySet(), users.findById(user.id).orElseThrow().allowedSections)
    }

    @Test
    fun `replaces and clears allowed sections`() {
        val user = users.saveAndFlush(newUser(setOf(AdminSection.DASHBOARDS)))
        entityManager.clear()
        val loaded = users.findById(user.id).orElseThrow()
        loaded.allowedSections = mutableSetOf(AdminSection.UPLOAD_DATA, AdminSection.MODEL_TRAINING)
        users.flush()
        entityManager.clear()

        val updated = users.findById(user.id).orElseThrow()
        assertEquals(setOf(AdminSection.UPLOAD_DATA, AdminSection.MODEL_TRAINING), updated.allowedSections)
        updated.allowedSections.clear()
        users.flush()
        entityManager.clear()

        assertEquals(emptySet(), users.findById(user.id).orElseThrow().allowedSections)
    }

    @Test
    fun `database rejects duplicate section for same user`() {
        val user = users.saveAndFlush(newUser(setOf(AdminSection.UPLOAD_DATA)))

        assertFailsWith<DataIntegrityViolationException> {
            jdbc.update("INSERT INTO admin_user_sections (admin_user_id, section) VALUES (?, ?)",
                user.id, "UPLOAD_DATA")
        }
    }

    @Test
    fun `database rejects section for missing user`() {
        assertFailsWith<DataIntegrityViolationException> {
            jdbc.update("INSERT INTO admin_user_sections (admin_user_id, section) VALUES (?, ?)",
                -1L, "UPLOAD_DATA")
        }
    }

    @Test
    fun `database deletes sections when user is deleted directly`() {
        val user = users.saveAndFlush(newUser(setOf(AdminSection.UPLOAD_DATA)))

        jdbc.update("DELETE FROM admin_users WHERE id = ?", user.id)

        assertEquals(0L, jdbc.queryForObject(
            "SELECT count(*) FROM admin_user_sections WHERE admin_user_id = ?", Long::class.java, user.id))
    }

    private fun newUser(sections: Set<AdminSection> = emptySet()): AdminUserEntity =
        AdminUserEntity(
            email = "sections-test@cardio.test",
            passwordHash = "unused-test-hash",
            allowedSections = sections.toMutableSet()
        )
}
