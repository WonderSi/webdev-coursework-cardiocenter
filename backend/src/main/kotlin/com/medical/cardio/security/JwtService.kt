package com.medical.cardio.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey
import java.util.Base64
import com.medical.cardio.exception.TokenExpiredException
import com.medical.cardio.exception.InvalidTokenException

@Service
class JwtService {

    @Value("\${app.jwt.secret}")
    private lateinit var secret: String

    @Value("\${app.jwt.expiration-ms}")
    private var expirationMs: Long = 0


    /**
     * Ключ декодируется из Base64
     */
    private val signingKey: SecretKey by lazy {
        val keyBytes = Base64.getDecoder().decode(secret)
        require(keyBytes.size >= 32) {
            "JWT secret must be at least 256 bits (32 bytes)"
        }
        Keys.hmacShaKeyFor(keyBytes)
    }

    /**
     * Генерирует токен, включая роли пользователя в claims
     */
    fun generateToken(userDetails: UserDetails): String {
        val claims = mapOf(
            "roles" to userDetails.authorities.map { it.authority }
        )
        return generateToken(claims, userDetails)
    }

    fun generateToken(extraClaims: Map<String, Any>, userDetails: UserDetails): String {
        return Jwts.builder()
            .claims(extraClaims)                                         // Роли
            .subject(userDetails.username)                               // Username
            .issuedAt(Date())                                            // Когда выдан
            .expiration(Date(System.currentTimeMillis() + expirationMs)) // Когда истечет
            .signWith(signingKey)
            .compact()
    }

    // Парсинг и оработка ошибок
    private fun extractAllClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw TokenExpiredException("JWT token has expired")
        } catch (e: JwtException) {
            throw InvalidTokenException("JWT token is invalid: ${e.message}")
        }
    }

    /*
        Извлечение данных из токена:
        - Username (subject)
        - Роли (claims)
    */
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T =
        claimsResolver(extractAllClaims(token))

    fun extractUsername(token: String): String =
        extractClaim(token, Claims::getSubject)

    @Suppress("UNCHECKED_CAST")
    fun extractRoles(token: String): List<String> =
        extractClaim(token) { claims ->
            claims["roles"] as? List<String> ?: emptyList()
        }

    /*
        Проверка токена:
        - Извлекаем username и сравниваем с userDetails
        - Проверяем, что токен не истек    
    */

    private fun isTokenExpired(token: String): Boolean =
        extractClaim(token, Claims::getExpiration).before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
}
