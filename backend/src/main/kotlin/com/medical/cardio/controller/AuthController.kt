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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
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
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.email, request.password)
            )
            val userDetails = adminUserDetailsService.loadUserByUsername(request.email)
            val token = jwtService.generateToken(userDetails)
            val cookieValue = buildString {
                append("access_token=$token; HttpOnly; Path=/; SameSite=Strict")
                if (request.rememberMe) append("; Max-Age=2592000")
            }

            ResponseEntity.ok()
                .header("Set-Cookie", cookieValue)
                .body(
                    LoginResponse(
                        email = userDetails.username,
                        role = userDetails.authorities.first().authority
                    )
                )
        } catch (_: BadCredentialsException) {
            ResponseEntity.status(401).build()
        }

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal user: UserDetails): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(
            LoginResponse(
                email = user.username,
                role = user.authorities.first().authority
            )
        )

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> =
        ResponseEntity.ok()
            .header(
                "Set-Cookie",
                "access_token=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict"
            )
            .build()
}
