package com.medical.cardio.controller

import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.dto.LoginResponse
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService
) {

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<LoginResponse> =
        try {
            // Проверяет логин/пароль
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.username, request.password)
            )
            // Загрузка пользователя из БД и генерация JWT-токена
            val userDetails = adminUserDetailsService.loadUserByUsername(request.username)
            val token = jwtService.generateToken(userDetails)

            ResponseEntity.ok(
                LoginResponse(
                    token = token,
                    username = userDetails.username,
                    role = userDetails.authorities.first().authority
                )
            )
        } catch (_: BadCredentialsException) {
            ResponseEntity.status(401).build()
        }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> =
        ResponseEntity.ok(Unit)
}
