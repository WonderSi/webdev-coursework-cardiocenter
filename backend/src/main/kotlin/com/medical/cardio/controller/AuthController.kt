package com.medical.cardio.controller

import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.dto.LoginResponse
import com.medical.cardio.security.AuthCookieService
import com.medical.cardio.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Авторизация", description = "Вход выдаёт HttpOnly cookie access_token и refresh_token. Для Swagger UI сначала выполните login на том же адресе Backend.")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val authCookieService: AuthCookieService
) {

    @Operation(
        summary = "Войти в систему",
        description = "Выдаёт access_token (Path=/) и refresh_token (Path=/api/auth): HttpOnly, SameSite=Strict, Secure в production. Без rememberMe cookie сессионные, refresh действует по умолчанию 1 день. С rememberMe cookie постоянные: access по умолчанию 15 минут, refresh 30 дней. Сроки настраиваются. Тело ответа содержит email и роль. Неверные данные и блокировка дают одинаковый 401. Обновление access через refresh будет добавлено отдельным этапом."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Вход выполнен; две cookie передаются отдельными заголовками Set-Cookie",
            useReturnTypeSchema = true,
            headers = [
                Header(
                    name = "Set-Cookie",
                    description = "Отдельные Set-Cookie для access_token и refresh_token",
                    schema = Schema(type = "string")
                )
            ]
        ),
        ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
    ])
    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<LoginResponse> {
        val result = authService.login(request)
        val accessCookie = authCookieService.accessToken(result.accessToken, request.rememberMe)
        val refreshCookie = authCookieService.refreshToken(result.refreshToken.rawToken, request.rememberMe)

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
            .body(result.user)
    }

    @Operation(summary = "Получить текущего пользователя", description = "Возвращает email и роль пользователя из действующей cookie.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Текущий пользователь", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized")
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
        description = "Удаляет access_token и refresh_token через отдельные Set-Cookie с Max-Age=0 и исходными Path. Серверный отзыв refresh относится к F-04 и пока не реализован. Запрос доступен без авторизации."
    )
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Cookie удалена, тело ответа отсутствует",
            content = [Content()],
            headers = [
                Header(
                    name = "Set-Cookie",
                    description = "access_token и refresh_token с Max-Age=0",
                    schema = Schema(type = "string")
                )
            ]
        )
    ])
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> =
        ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE,
                authCookieService.clearAccessToken().toString(),
                authCookieService.clearRefreshToken().toString()
            )
            .build()
}
