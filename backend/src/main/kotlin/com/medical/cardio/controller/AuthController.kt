package com.medical.cardio.controller

import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.dto.LoginResponse
import com.medical.cardio.security.AdminUserDetailsService
import com.medical.cardio.security.JwtService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Авторизация", description = "Вход и выход через HttpOnly cookie access_token. Для Swagger UI сначала выполните login на том же адресе Backend.")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService
) {

    @Operation(
        summary = "Войти в систему",
        description = "При успехе браузер сохраняет access_token (HttpOnly, SameSite=Strict, Path=/). rememberMe сохраняет cookie на 30 дней, но не продлевает срок самого JWT. Неверные данные и блокировка дают одинаковый 401."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Вход выполнен; cookie передаётся в Set-Cookie",
            useReturnTypeSchema = true,
            headers = [
                Header(
                    name = "Set-Cookie",
                    description = "HttpOnly cookie access_token",
                    schema = Schema(type = "string")
                )
            ]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Некорректные поля запроса",
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(type = "object"),
                    examples = [
                        ExampleObject(
                            name = "validationError",
                            value = """{"errors":{"email":"must be a well-formed email address"}}"""
                        ),
                        ExampleObject(
                            name = "badRequest",
                            value = """{"error":"Некорректный запрос"}"""
                        )
                    ]
                )
            ]
        ),
        ApiResponse(responseCode = "401", description = "Неверные данные или учётная запись заблокирована", content = [Content()])
    ])
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
        } catch (_: DisabledException) {
            ResponseEntity.status(401).build()
        }

    @Operation(summary = "Получить текущего пользователя", description = "Возвращает email и роль пользователя из действующей cookie.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Текущий пользователь", useReturnTypeSchema = true),
        ApiResponse(
            responseCode = "401",
            description = "Нет действующей авторизации: cookie отсутствует, токен истёк или учётная запись заблокирована",
            content = [Content()]
        )
    ])
    @GetMapping("/me")
    fun me(@Parameter(hidden = true) @AuthenticationPrincipal user: UserDetails): ResponseEntity<LoginResponse> =
        ResponseEntity.ok(
            LoginResponse(
                email = user.username,
                role = user.authorities.first().authority
            )
        )

    @Operation(
        summary = "Выйти из системы",
        description = "Удаляет cookie access_token через Set-Cookie с Max-Age=0. Серверного отзыва уже выданного JWT нет. Запрос доступен без авторизации."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Cookie удалена, тело ответа отсутствует",
            content = [Content()],
            headers = [
                Header(
                    name = "Set-Cookie",
                    description = "access_token с Max-Age=0",
                    schema = Schema(type = "string")
                )
            ]
        )
    ])
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> =
        ResponseEntity.ok()
            .header(
                "Set-Cookie",
                "access_token=; HttpOnly; Path=/; Max-Age=0; SameSite=Strict"
            )
            .build()
}
