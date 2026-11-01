package com.medical.cardio.repository

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.RefreshTokenEntity
import com.medical.cardio.entity.Role
import jakarta.persistence.EntityManager
import org.springframework.transaction.annotation.Transactional
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class RefreshTokenRepositoryTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `findByTokenHash returns null for unknown has`() {
        val result = refreshTokenRepository.findByTokenHash("unknown-token-hash")
        assertNull(result)
    }

    @Transactional
    @Test
    fun `findByTokenHash returns saved session with expected fields`() {
        val user = adminUserRepository.save(
            AdminUserEntity(
                email = "refresh-test@cardio.test",
                passwordHash = passwordEncoder.encode("test-password-hash"),
                role = Role.DOCTOR
            )
        )

        val created = Instant.parse("2026-11-02T10:00:00Z")

        val savedToken = refreshTokenRepository.saveAndFlush(
            RefreshTokenEntity(
                creator = user,
                tokenHash = "test-refresh-token-hash",
                createdDate = created,
                expiresDate = created.plusSeconds(3600)
            )
        )

        entityManager.clear()

        val found = refreshTokenRepository.findByTokenHash(savedToken.tokenHash)

        assertNotNull(found)
        assertEquals(savedToken.id, found.id)
        assertEquals(user.id, found.creator.id)
        assertEquals("test-refresh-token-hash", found.tokenHash)
        assertEquals(created, found.createdDate)
        assertEquals(created.plusSeconds(3600), found.expiresDate)
        assertNull(found.revokedDate)
    }

}
