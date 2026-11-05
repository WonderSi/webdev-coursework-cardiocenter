package com.medical.cardio.repository

import com.medical.cardio.entity.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE RefreshTokenEntity session
        SET session.revokedDate = :revokedDate
        WHERE session.tokenHash = :tokenHash AND session.revokedDate IS NULL
    """)
    fun revokeByTokenHash(
        @Param("tokenHash") tokenHash: String,
        @Param("revokedDate") revokedDate: Instant
    ): Int
}
