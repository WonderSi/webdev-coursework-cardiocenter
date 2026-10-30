package com.medical.cardio.exception

import java.net.URI
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import com.medical.cardio.dto.FieldErrorDetail


object ApiProblem {
    const val VALIDATION_ERROR = "VALIDATION_ERROR"
    const val BAD_REQUEST = "BAD_REQUEST"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FORBIDDEN = "FORBIDDEN"
    const val NOT_FOUND = "NOT_FOUND"
    const val INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR"
    const val PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE"
    const val TOKEN_EXPIRED = "TOKEN_EXPIRED" 
    const val INVALID_TOKEN = "INVALID_TOKEN"
    const val ML_SERVICE_ERROR = "ML_SERVICE_ERROR"
    const val METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED"
    const val NOT_ACCEPTABLE = "NOT_ACCEPTABLE"
    const val UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE"
    const val SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE"

    fun forStatus(status: HttpStatusCode): ProblemDetail {
        val (type, title, detail, code) = when (status.value()) {
            400 -> listOf("bad-request", "Некорректный запрос", "Некорректный запрос.", BAD_REQUEST)
            401 -> listOf("authentication", "Требуется авторизация", "Войдите в систему для выполнения запроса.", UNAUTHORIZED)
            403 -> listOf("forbidden", "Доступ запрещён", "Недостаточно прав для выполнения запроса.", FORBIDDEN)
            404 -> listOf("not-found", "Ресурс не найден", "Запрашиваемый ресурс не найден.", NOT_FOUND)
            405 -> listOf("method-not-allowed", "Метод не поддерживается", "HTTP-метод не поддерживается для этого ресурса.", METHOD_NOT_ALLOWED)
            406 -> listOf("not-acceptable", "Формат ответа не поддерживается", "Запрошенный формат ответа не поддерживается.", NOT_ACCEPTABLE)
            413 -> listOf("payload-too-large", "Превышен размер файла", "Файл превышает допустимый размер 10 МБ.", PAYLOAD_TOO_LARGE)
            415 -> listOf("unsupported-media-type", "Формат запроса не поддерживается", "Проверьте Content-Type запроса.", UNSUPPORTED_MEDIA_TYPE)
            503 -> listOf("service-unavailable", "Сервис временно недоступен", "Сервис временно не может выполнить запрос.", SERVICE_UNAVAILABLE)
            else -> if (status.is5xxServerError) {
                listOf("internal-server-error", "Внутренняя ошибка сервера", "Не удалось выполнить запрос. Попробуйте позже.", INTERNAL_SERVER_ERROR)
            } else {
                listOf("bad-request", "Некорректный запрос", "Некорректный запрос.", BAD_REQUEST)
            }
        }
        return create(status, "urn:cardiocenter:problem:$type", title, detail, code)
    }

    fun create(
        status: HttpStatusCode,
        type: String,
        title: String,
        detail: String,
        code: String,
        errors: List<FieldErrorDetail>? = null
    ): ProblemDetail {
        val problem = ProblemDetail.forStatusAndDetail(status, detail)
        problem.type = URI.create(type)
        problem.title = title
        problem.setProperty("code", code)
        if (errors != null) {
            problem.setProperty("errors", errors)
        }
        return problem
    }
}
