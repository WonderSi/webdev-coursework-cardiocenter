package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

@Transactional
class RefreshTokenServiceTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var refreshTokenService: RefreshTokenService

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun `issueToken persists hash and session metadata and returns raw token`() {
        val user = createUser()
        val lifetime = Duration.ofDays(1)
        val sessionCount = refreshTokenRepository.count()
        val beforeIssuance = Instant.now()

        val issuedToken = refreshTokenService.issueToken(user, lifetime)
        val afterIssuance = Instant.now()

        entityManager.flush()
        entityManager.clear()

        val storedSession = requireNotNull(
            refreshTokenRepository.findByTokenHash(sha256(issuedToken.rawToken))
        )

        assertEquals(sessionCount + 1, refreshTokenRepository.count())
        assertEquals(user.id, storedSession.creator.id)
        assertNotEquals(issuedToken.rawToken, storedSession.tokenHash)
        assertEquals(32, Base64.getUrlDecoder().decode(issuedToken.rawToken).size)
        assertNull(storedSession.revokedDate)
        assertEquals(lifetime, Duration.between(storedSession.createdDate, storedSession.expiresDate))

        // PostgreSQL TIMESTAMPTZ stores microseconds; Java Instant also contains nanoseconds.
        val databasePrecision = Duration.ofNanos(1_000)
        assertTrue(
            Duration.between(issuedToken.expiresDate, storedSession.expiresDate).abs() <= databasePrecision
        )
        assertTrue(storedSession.createdDate >= beforeIssuance.minus(databasePrecision))
        assertTrue(storedSession.createdDate <= afterIssuance.plus(databasePrecision))
    }

    @Test
    fun `issueToken creates independent sessions for the same user`() {
        val user = createUser()
        val sessionCount = refreshTokenRepository.count()

        val firstToken = refreshTokenService.issueToken(user, Duration.ofDays(1))
        val secondToken = refreshTokenService.issueToken(user, Duration.ofDays(1))

        entityManager.flush()
        entityManager.clear()

        val firstSession = requireNotNull(
            refreshTokenRepository.findByTokenHash(sha256(firstToken.rawToken))
        )
        val secondSession = requireNotNull(
            refreshTokenRepository.findByTokenHash(sha256(secondToken.rawToken))
        )

        assertNotEquals(firstToken.rawToken, secondToken.rawToken)
        assertNotEquals(firstSession.tokenHash, secondSession.tokenHash)
        assertNotEquals(firstSession.id, secondSession.id)
        assertEquals(user.id, firstSession.creator.id)
        assertEquals(user.id, secondSession.creator.id)
        assertEquals(sessionCount + 2, refreshTokenRepository.count())
    }

    @Test
    fun `issueToken rejects zero lifetime without saving a session`() {
        assertLifetimeRejected(Duration.ZERO)
    }

    @Test
    fun `issueToken rejects negative lifetime without saving a session`() {
        assertLifetimeRejected(Duration.ofSeconds(-1))
    }

    private fun assertLifetimeRejected(lifetime: Duration) {
        val user = createUser()
        val sessionCount = refreshTokenRepository.count()

        assertThrows<IllegalArgumentException> {
            refreshTokenService.issueToken(user, lifetime)
        }

        assertEquals(sessionCount, refreshTokenRepository.count())
    }

    private fun createUser(): AdminUserEntity = adminUserRepository.save(
        AdminUserEntity(
            email = "refresh-service-test@cardio.test",
            passwordHash = passwordEncoder.encode("TestPassword@2000")
        )
    )

    private fun sha256(rawToken: String): String {
        val hashBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(hashBytes)
    }
}
