package com.medical.cardio.exception

import java.net.URI
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
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

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Исправьте отмеченные поля."
        )

        problem.type = URI.create("urn:cardiocenter:problem:validation")
        problem.title = "Ошибка валидации"
        problem.setProperty("code", "VALIDATION_ERROR")
        problem.setProperty("errors", errors)

        return problem
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail {
        log.warn("IllegalArgumentException: ${ex.message}")

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Некорректный запрос."
        )

        problem.type = URI.create("urn:cardiocenter:problem:bad-request")
        problem.title = "Некорректный запрос"
        problem.setProperty("code", "BAD_REQUEST")

        return problem

    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleFileTooLarge(ex: MaxUploadSizeExceededException): ProblemDetail {
        log.warn("MaxUploadSizeExceededException: ${ex.message}")

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "Файл превышает допустимый размер 10 МБ."
        )

        problem.type = URI.create("urn:cardiocenter:problem:payload-too-large")
        problem.title = "Превышен размер файла"
        problem.setProperty("code", "PAYLOAD_TOO_LARGE")

        return problem
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ProblemDetail {
        log.warn("NoSuchElementException: ${ex.message}")

        val problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "Запрашиваемый ресурс не найден."
        )

        problem.type = URI.create("urn:cardiocenter:problem:not-found")
        problem.title = "Ресурс не найден"
        problem.setProperty("code", "NOT_FOUND")

        return problem
    }
}
