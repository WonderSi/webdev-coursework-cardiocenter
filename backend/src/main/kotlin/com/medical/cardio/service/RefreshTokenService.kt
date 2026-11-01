package com.medical.cardio.service

import com.medical.cardio.dto.IssuedRefreshToken
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.RefreshTokenEntity
import com.medical.cardio.repository.RefreshTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.HexFormat

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val secureRandom = SecureRandom()

    private fun generateRawToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(rawToken: String): String {
        val tokenBytes = rawToken.toByteArray(Charsets.UTF_8)

        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(tokenBytes)
        val tokenHash = HexFormat.of().formatHex(hashBytes)
        return tokenHash
    }

    @Transactional
    fun issueToken(user: AdminUserEntity, lifetime: Duration): IssuedRefreshToken {
        require(!lifetime.isZero && !lifetime.isNegative) {
            "Refresh token lifetime must be positive"
        }

        val rawToken = generateRawToken()
        val tokenHash = hashToken(rawToken)
        val createdDate = Instant.now()
        val expiresDate = createdDate.plus(lifetime)

        val refreshSession = RefreshTokenEntity(
            creator = user,
            tokenHash = tokenHash,
            createdDate = createdDate,
            expiresDate = expiresDate
        )

        refreshTokenRepository.save(refreshSession)

        return IssuedRefreshToken(
            rawToken = rawToken,
            expiresDate = expiresDate
        )
    }
}
