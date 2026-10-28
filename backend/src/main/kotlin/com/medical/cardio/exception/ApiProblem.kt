package com.medical.cardio.exception

import java.net.URI
import org.springframework.http.HttpStatus
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

    fun create(
        status: HttpStatus,
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
