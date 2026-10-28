package com.medical.cardio.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import com.medical.cardio.dto.FieldErrorDetail


@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ProblemDetail {
        log.warn("Validation failed: {} field errors", ex.bindingResult.fieldErrors.size)

        val errors = ex.bindingResult.fieldErrors.map {
            FieldErrorDetail(field = it.field, message = it.defaultMessage ?: "Некорректное значение")
        }

        return ApiProblem.create(
            status = HttpStatus.BAD_REQUEST,
            type = "urn:cardiocenter:problem:validation",
            title = "Ошибка валидации",
            detail = "Исправьте отмеченные поля.",
            code = ApiProblem.VALIDATION_ERROR,
            errors = errors
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("IllegalArgumentException: ${ex.message}")

        return ApiProblem.create(
            status = HttpStatus.BAD_REQUEST,
            type = "urn:cardiocenter:problem:bad-request",
            title = "Некорректный запрос",
            detail = "Некорректный запрос.",
            code = ApiProblem.BAD_REQUEST
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleFileTooLarge(ex: MaxUploadSizeExceededException): ProblemDetail {
        log.warn("MaxUploadSizeExceededException: ${ex.message}")

        return ApiProblem.create(
            status = HttpStatus.PAYLOAD_TOO_LARGE,
            type = "urn:cardiocenter:problem:payload-too-large",
            title = "Превышен размер файла",
            detail = "Файл превышает допустимый размер 10 МБ.",
            code = ApiProblem.PAYLOAD_TOO_LARGE
        )
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ProblemDetail {
        log.warn("NoSuchElementException: ${ex.message}")

        return ApiProblem.create(
            status = HttpStatus.NOT_FOUND,
            type = "urn:cardiocenter:problem:not-found",
            title = "Ресурс не найден",
            detail = "Запрашиваемый ресурс не найден.",
            code = ApiProblem.NOT_FOUND
        )
    }

    @ExceptionHandler(
        BadCredentialsException::class,
        DisabledException::class
    )
    fun handleLoginFailure(): ProblemDetail {
        log.warn("Login failed")

        return ApiProblem.create(
            status = HttpStatus.UNAUTHORIZED,
            type = "urn:cardiocenter:problem:authentication",
            title = "Ошибка входа",
            detail = "Не удалось войти. Проверьте email и пароль.",
            code = ApiProblem.UNAUTHORIZED
        )
    }
}
