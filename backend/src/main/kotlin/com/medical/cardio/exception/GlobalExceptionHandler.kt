package com.medical.cardio.exception

import com.medical.cardio.dto.FieldErrorDetail
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errors = when {
            ex is MethodArgumentNotValidException ->
                ex.bindingResult.fieldErrors.map {
                    FieldErrorDetail(it.field, it.defaultMessage ?: "Некорректное значение")
                } + ex.bindingResult.globalErrors.map {
                    FieldErrorDetail("_global", it.defaultMessage ?: "Некорректные данные")
                }
            ex is HandlerMethodValidationException && !ex.isForReturnValue ->
                ex.parameterValidationResults.flatMap { result ->
                    val field = result.methodParameter.parameterName
                        ?: "arg${result.methodParameter.parameterIndex}"
                    result.resolvableErrors.map {
                        FieldErrorDetail(field, it.defaultMessage ?: "Некорректное значение")
                    }
                }
            else -> null
        }
        val problem = if (errors != null) {
            ApiProblem.create(
                HttpStatus.BAD_REQUEST, "urn:cardiocenter:problem:validation",
                "Ошибка валидации", "Исправьте отмеченные поля.", ApiProblem.VALIDATION_ERROR, errors
            )
        } else {
            ApiProblem.forStatus(statusCode)
        }

        if (statusCode.is5xxServerError) {
            log.error("MVC request failed with status {}", statusCode.value(), ex)
        } else {
            log.warn("MVC request rejected: {}, status {}", ex.javaClass.simpleName, statusCode.value())
        }
        return super.handleExceptionInternal(ex, problem, headers, statusCode, request)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(): ProblemDetail {
        log.warn("Request rejected: IllegalArgumentException")
        return ApiProblem.forStatus(HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(): ProblemDetail {
        log.warn("Requested resource not found")
        return ApiProblem.forStatus(HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleLoginFailure(): ProblemDetail {
        log.warn("Login failed")
        return ApiProblem.create(
            HttpStatus.UNAUTHORIZED, "urn:cardiocenter:problem:authentication",
            "Ошибка входа", "Не удалось войти. Проверьте email и пароль.", ApiProblem.UNAUTHORIZED
        )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(): ProblemDetail = ApiProblem.forStatus(HttpStatus.FORBIDDEN)

    @ExceptionHandler(MlServiceException::class)
    fun handleMlService(ex: MlServiceException): ProblemDetail {
        log.error("ML service operation failed", ex)
        return ApiProblem.create(
            HttpStatus.BAD_GATEWAY, "urn:cardiocenter:problem:ml-service",
            "Ошибка ML-сервиса", "Не удалось выполнить операцию ML-сервиса.", ApiProblem.ML_SERVICE_ERROR
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ProblemDetail {
        log.error("Unexpected request failure", ex)
        return ApiProblem.forStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
