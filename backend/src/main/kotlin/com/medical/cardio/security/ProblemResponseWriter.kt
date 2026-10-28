package com.medical.cardio.security

import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.http.ProblemDetail
import org.springframework.http.MediaType


@Component
class ProblemResponseWriter (
    private val objectMapper: ObjectMapper
) {
    fun write(
        request: HttpServletRequest,
        response: HttpServletResponse,
        problem: ProblemDetail
    ) {
        problem.instance = URI.create(request.requestURI)

        response.status = problem.status
        response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
        response.characterEncoding = "UTF-8"

        objectMapper.writeValue(response.writer, problem)
    }
}