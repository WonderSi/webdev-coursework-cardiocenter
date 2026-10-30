package com.medical.cardio.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProblemOpenApiConfig {
    @Bean
    fun problemDetailsDocumentation(): OpenApiCustomizer = OpenApiCustomizer { document ->
        val components = document.components ?: Components().also { document.components = it }
        components.addSchemas("FieldErrorDetail", ObjectSchema()
            .addProperty("field", StringSchema().example("age"))
            .addProperty("message", StringSchema().example("must be greater than or equal to 18"))
            .required(listOf("field", "message")))
        components.addSchemas("ApiProblem", ObjectSchema()
            .description("Problem Details. code — стабильный машинный код; errors присутствует только при валидации.")
            .addProperty("type", StringSchema().format("uri").example("urn:cardiocenter:problem:validation"))
            .addProperty("title", StringSchema())
            .addProperty("status", IntegerSchema().format("int32").minimum(java.math.BigDecimal(400)).maximum(java.math.BigDecimal(599)))
            .addProperty("detail", StringSchema())
            .addProperty("instance", StringSchema().format("uri-reference").example("/api/survey/submit"))
            .addProperty("code", StringSchema())
            .addProperty("errors", ArraySchema().items(Schema<Any>().`$ref`("#/components/schemas/FieldErrorDetail")))
            .required(listOf("type", "title", "status", "detail", "instance", "code")))

        val definitions = listOf(
            Definition(400, "BadRequest", "Некорректный запрос или ошибки валидации", "bad-request", "Некорректный запрос", "Некорректный запрос.", "BAD_REQUEST"),
            Definition(401, "Unauthorized", "Требуется вход: cookie отсутствует, JWT истёк/недействителен либо вход отклонён", "authentication", "Требуется авторизация", "Войдите в систему для выполнения запроса.", "UNAUTHORIZED"),
            Definition(403, "Forbidden", "Недостаточно прав", "forbidden", "Доступ запрещён", "Недостаточно прав для выполнения запроса.", "FORBIDDEN"),
            Definition(404, "NotFound", "Ресурс не найден", "not-found", "Ресурс не найден", "Запрашиваемый ресурс не найден.", "NOT_FOUND"),
            Definition(405, "MethodNotAllowed", "HTTP-метод не поддерживается", "method-not-allowed", "Метод не поддерживается", "HTTP-метод не поддерживается для этого ресурса.", "METHOD_NOT_ALLOWED"),
            Definition(406, "NotAcceptable", "Формат ответа не поддерживается", "not-acceptable", "Формат ответа не поддерживается", "Запрошенный формат ответа не поддерживается.", "NOT_ACCEPTABLE"),
            Definition(413, "PayloadTooLarge", "Превышен размер файла", "payload-too-large", "Превышен размер файла", "Файл превышает допустимый размер 10 МБ.", "PAYLOAD_TOO_LARGE"),
            Definition(415, "UnsupportedMediaType", "Content-Type не поддерживается", "unsupported-media-type", "Формат запроса не поддерживается", "Проверьте Content-Type запроса.", "UNSUPPORTED_MEDIA_TYPE"),
            Definition(500, "InternalServerError", "Непредвиденная ошибка; технические сведения доступны только в серверных логах", "internal-server-error", "Внутренняя ошибка сервера", "Не удалось выполнить запрос. Попробуйте позже.", "INTERNAL_SERVER_ERROR"),
            Definition(502, "MlServiceError", "Ошибка ML: сетевой сбой, таймаут, HTTP-ошибка, некорректный или пустой ответ", "ml-service", "Ошибка ML-сервиса", "Не удалось выполнить операцию ML-сервиса.", "ML_SERVICE_ERROR")
        )
        definitions.forEach { definition ->
            val media = MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ApiProblem"))
                .addExamples("problem", Example().value(definition.example()))
            if (definition.status == 400) {
                media.addExamples("validation", Example().value(mapOf(
                    "type" to "urn:cardiocenter:problem:validation", "title" to "Ошибка валидации",
                    "status" to 400, "detail" to "Исправьте отмеченные поля.",
                    "instance" to "/api/survey/submit", "code" to "VALIDATION_ERROR",
                    "errors" to listOf(mapOf("field" to "age", "message" to "must be greater than or equal to 18"))
                )))
            }
            if (definition.status == 401) {
                media.addExamples("loginFailed", Example().value(mapOf(
                    "type" to "urn:cardiocenter:problem:authentication", "title" to "Ошибка входа",
                    "status" to 401, "detail" to "Не удалось войти. Проверьте email и пароль.",
                    "instance" to "/api/auth/login", "code" to "UNAUTHORIZED"
                )))
                listOf("token-expired" to "TOKEN_EXPIRED", "invalid-token" to "INVALID_TOKEN").forEach { (type, code) ->
                    media.addExamples(type, Example().value(mapOf(
                        "type" to "urn:cardiocenter:problem:$type",
                        "title" to if (code == "TOKEN_EXPIRED") "Сессия истекла" else "Недействительный токен",
                        "status" to 401, "detail" to "Войдите в систему снова.",
                        "instance" to "/api/auth/me", "code" to code
                    )))
                }
            }
            components.addResponses(definition.name, ApiResponse().description(definition.description)
                .content(Content().addMediaType("application/problem+json", media)))
        }

        document.paths?.values?.forEach { path ->
            path.readOperations().forEach { operation ->
                val responses = operation.responses ?: ApiResponses().also { operation.responses = it }
                fun add(status: String, name: String) {
                    if (!responses.containsKey(status)) {
                        responses.addApiResponse(status, ApiResponse().`$ref`("#/components/responses/$name"))
                    }
                }
                add("500", "InternalServerError")
                add("405", "MethodNotAllowed")
                add("406", "NotAcceptable")
                if (operation.requestBody != null) {
                    add("400", "BadRequest")
                    add("415", "UnsupportedMediaType")
                }
                if (operation.security?.any { it.containsKey("cookieAuth") } == true) {
                    add("401", "Unauthorized")
                    add("403", "Forbidden")
                }
            }
        }
    }

    private data class Definition(
        val status: Int, val name: String, val description: String, val type: String,
        val title: String, val detail: String, val code: String
    ) {
        fun example(): Map<String, Any> = mapOf(
            "type" to "urn:cardiocenter:problem:$type", "title" to title,
            "status" to status, "detail" to detail, "instance" to "/api/survey/submit", "code" to code
        )
    }
}
