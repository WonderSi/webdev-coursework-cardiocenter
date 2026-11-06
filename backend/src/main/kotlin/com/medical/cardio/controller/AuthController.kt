package com.medical.cardio.controller

import com.medical.cardio.dto.LoginRequest
import com.medical.cardio.dto.LoginResponse
import com.medical.cardio.dto.CsrfTokenResponse
import com.medical.cardio.security.AuthCookieService
import com.medical.cardio.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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
import org.springframework.http.CacheControl
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.CookieValue
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
        description = "Выдаёт access_token (Path=/) и refresh_token (Path=/api/auth): HttpOnly, SameSite=Strict, Secure в production. Без rememberMe cookie сессионные, refresh действует по умолчанию 1 день. С rememberMe cookie постоянные: access по умолчанию 15 минут, refresh 30 дней. Сроки настраиваются. Тело ответа содержит email и роль. Неверные данные и блокировка дают одинаковый 401. Для обновления access используйте POST /api/auth/refresh с CSRF-токеном из GET /api/auth/csrf."
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
        ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
        ApiResponse(responseCode = "429", ref = "#/components/responses/LoginRateLimited")
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

    @Operation(
        summary = "Получить CSRF-токен для обновления доступа",
        description = "Сохраняет HttpOnly cookie XSRF-TOKEN с Path=/api/auth и возвращает token и headerName. Отправляйте token в заголовке headerName вместе с cookie при POST /api/auth/refresh и POST /api/auth/logout. Не требует действующего access. Ответ запрещено кэшировать."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "CSRF-токен и имя заголовка", useReturnTypeSchema = true)
    ])
    @GetMapping("/csrf")
    fun csrf(@Parameter(hidden = true) csrfToken: CsrfToken): ResponseEntity<CsrfTokenResponse> =
        ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(CsrfTokenResponse(headerName = csrfToken.headerName, token = csrfToken.token))

    @Operation(
        summary = "Обновить access token по refresh-сессии",
        description = "Проверяет refresh_token, его срок, отзыв и блокировку сотрудника. Возвращает текущие email и роль и новую access cookie; режим rememberMe берётся из сессии. Работает без access или с истёкшим/недействительным access. Refresh не заменяется и его исходный срок не продлевается. Сначала получите CSRF-токен через GET /api/auth/csrf.",
        parameters = [Parameter(
            name = "X-XSRF-TOKEN", `in` = ParameterIn.HEADER, required = true,
            description = "Значение token из GET /api/auth/csrf; браузер также передаёт XSRF-TOKEN cookie"
        )]
    )
    @SecurityRequirement(name = "refreshCookieAuth")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Новый access_token в Set-Cookie", useReturnTypeSchema = true),
        ApiResponse(responseCode = "401", ref = "#/components/responses/InvalidRefresh"),
        ApiResponse(responseCode = "403", ref = "#/components/responses/CsrfRejected")
    ])
    @PostMapping("/refresh")
    fun refresh(
        @Parameter(hidden = true) @CookieValue(name = "refresh_token", required = false) rawRefreshToken: String?
    ): ResponseEntity<LoginResponse> {
        val result = authService.refresh(rawRefreshToken)
        val accessCookie = authCookieService.accessToken(result.accessToken, result.rememberMe)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
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
        description = "Отзывает только refresh из текущей cookie и сохраняет время первого отзыва, затем удаляет access_token и refresh_token с исходными Path и флагами. Повторный выход и отсутствующий, неизвестный или истёкший refresh завершаются успешно. Действующий access не требуется. Нужен CSRF-токен из GET /api/auth/csrf. Другие сессии сотрудника продолжают работать. Копия уже выданного access может действовать до своего истечения.",
        parameters = [Parameter(
            name = "X-XSRF-TOKEN", `in` = ParameterIn.HEADER, required = true,
            description = "Значение token из GET /api/auth/csrf вместе с XSRF-TOKEN cookie"
        )]
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
        ),
        ApiResponse(responseCode = "403", ref = "#/components/responses/CsrfRejected")
    ])
    @PostMapping("/logout")
    fun logout(
        @Parameter(hidden = true) @CookieValue(name = "refresh_token", required = false) rawRefreshToken: String?
    ): ResponseEntity<Unit> {
        authService.logout(rawRefreshToken)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(
                HttpHeaders.SET_COOKIE,
                authCookieService.clearAccessToken().toString(),
                authCookieService.clearRefreshToken().toString()
            )
            .build()
    }
}
