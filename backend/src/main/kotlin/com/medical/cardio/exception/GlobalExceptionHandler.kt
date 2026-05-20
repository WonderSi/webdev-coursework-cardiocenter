package com.medical.cardio.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

data class ValidationErrorResponse(
    val errors: Map<String, String>
)

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ValidationErrorResponse> {
        val errors = ex.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }
        return ResponseEntity.badRequest().body(ValidationErrorResponse(errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        log.warn("Bad request: ${ex.message}")
        return ResponseEntity.badRequest().body(mapOf("error" to "Некорректный запрос"))
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleFileTooLarge(): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(mapOf("error" to "Файл превышает допустимый размер 10 МБ"))
}
