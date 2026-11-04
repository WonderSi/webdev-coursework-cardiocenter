package com.medical.cardio.service

import com.medical.cardio.BaseIntegrationTest
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.exception.InvalidRefreshTokenException
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.repository.RefreshTokenRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.HexFormat
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RefreshTokenRevocationTest : BaseIntegrationTest() {
    @Autowired private lateinit var service: RefreshTokenService
    @Autowired private lateinit var tokens: RefreshTokenRepository
    @Autowired private lateinit var users: AdminUserRepository
    @Autowired private lateinit var transactionManager: PlatformTransactionManager

    private lateinit var user: AdminUserEntity

    @BeforeEach
    fun setUp() {
        user = users.save(AdminUserEntity(email = "revocation-service@cardio.test", passwordHash = "test-hash"))
    }

    @AfterEach
    fun tearDown() {
        users.deleteById(user.id)
    }

    @Test
    fun `revokeToken stores time without changing expiry and prevents refresh`() {
        val issued = service.issueToken(user, Duration.ofDays(1))
        val original = requireNotNull(tokens.findByTokenHash(sha256(issued.rawToken)))
        val count = tokens.count()
        val before = Instant.now().minusMillis(1)

        service.revokeToken(issued.rawToken)

        val after = Instant.now().plusMillis(1)
        val revoked = requireNotNull(tokens.findByTokenHash(sha256(issued.rawToken)))
        val revokedDate = requireNotNull(revoked.revokedDate)
        assertEquals(original.id, revoked.id)
        assertEquals(original.expiresDate, revoked.expiresDate)
        assertTrue(revokedDate >= before && revokedDate <= after)
        assertEquals(count, tokens.count())
        assertThrows<InvalidRefreshTokenException> { service.validateToken(issued.rawToken) }
    }

    @Test
    fun `revokeToken preserves existing revocation time`() {
        val issued = service.issueToken(user, Duration.ofDays(1))
        val session = requireNotNull(tokens.findByTokenHash(sha256(issued.rawToken)))
        val originalDate = Instant.parse("2026-11-01T10:00:00Z")
        session.revokedDate = originalDate
        tokens.save(session)

        service.revokeToken(issued.rawToken)

        assertEquals(originalDate, tokens.findByTokenHash(sha256(issued.rawToken))!!.revokedDate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["missing", "empty", "blank", "unknown"])
    fun `missing or unknown token leaves other sessions unchanged`(state: String) {
        val issued = service.issueToken(user, Duration.ofDays(1))
        val count = tokens.count()
        val rawToken = when (state) {
            "missing" -> null
            "empty" -> ""
            "blank" -> "   "
            else -> "unknown-refresh-token"
        }

        service.revokeToken(rawToken)

        assertEquals(count, tokens.count())
        assertNull(tokens.findByTokenHash(sha256(issued.rawToken))!!.revokedDate)
    }

    @ParameterizedTest
    @ValueSource(strings = ["expired", "blocked"])
    fun `expired session and blocked account can still be revoked`(state: String) {
        val issued = service.issueToken(user, Duration.ofDays(1))
        if (state == "expired") {
            val session = tokens.findByTokenHash(sha256(issued.rawToken))!!
            session.expiresDate = Instant.now().minusSeconds(1)
            tokens.save(session)
        } else {
            user.isBlocked = true
            users.save(user)
        }

        service.revokeToken(issued.rawToken)

        assertNotNull(tokens.findByTokenHash(sha256(issued.rawToken))!!.revokedDate)
    }

    @Test
    fun `concurrent revocations update the row once and preserve the winning time`() {
        val issued = service.issueToken(user, Duration.ofDays(1))
        val hash = sha256(issued.rawToken)
        val dates = listOf(Instant.parse("2026-11-01T10:00:00Z"), Instant.parse("2026-11-02T10:00:00Z"))
        val barrier = CyclicBarrier(2)
        val executor = Executors.newFixedThreadPool(2)

        try {
            val attempts = dates.map { date ->
                executor.submit(Callable {
                    barrier.await(5, TimeUnit.SECONDS)
                    val updated = TransactionTemplate(transactionManager).execute {
                        tokens.revokeByTokenHash(hash, date)
                    }!!
                    updated to date
                })
            }.map { it.get(10, TimeUnit.SECONDS) }

            assertEquals(listOf(0, 1), attempts.map { it.first }.sorted())
            val winningDate = attempts.single { it.first == 1 }.second
            assertEquals(winningDate, tokens.findByTokenHash(hash)!!.revokedDate)
        } finally {
            executor.shutdownNow()
        }
    }

    private fun sha256(rawToken: String): String = HexFormat.of().formatHex(
        MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
    )
}
