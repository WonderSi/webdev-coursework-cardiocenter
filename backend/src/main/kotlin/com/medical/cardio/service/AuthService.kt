package com.medical.cardio.service

import com.medical.cardio.config.RefreshTokenConfig
import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.dto.LoginResponse
import com.medical.cardio.dto.LoginResult
import com.medical.cardio.dto.RefreshResult
import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val adminUserRepository: AdminUserRepository,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val refreshTokenConfig: RefreshTokenConfig
) {
    @Transactional
    fun login(request: LoginRequest): LoginResult {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val user = adminUserRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Invalid credentials")
        if (user.isBlocked) throw DisabledException("User is disabled")

        val accessToken = jwtService.generateToken(userDetails(user))
        val refreshToken = refreshTokenService.issueToken(
            user,
            refreshTokenConfig.lifetimeFor(request.rememberMe),
            rememberMe = request.rememberMe
        )

        return LoginResult(
            user = userResponse(user),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional(readOnly = true)
    fun refresh(rawRefreshToken: String?): RefreshResult {
        val session = refreshTokenService.validateToken(rawRefreshToken)
        val user = session.creator
        return RefreshResult(
            user = userResponse(user),
            accessToken = jwtService.generateToken(userDetails(user)),
            rememberMe = session.rememberMe
        )
    }

    @Transactional
    fun logout(rawRefreshToken: String?) {
        refreshTokenService.revokeToken(rawRefreshToken)
    }

    private fun userDetails(user: AdminUserEntity): UserDetails = User.withUsername(user.email)
        .password(user.passwordHash)
        .authorities("ROLE_${user.role}")
        .build()

    private fun userResponse(user: AdminUserEntity): LoginResponse =
        LoginResponse(email = user.email, role = "ROLE_${user.role}")
}
