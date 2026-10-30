package com.medical.cardio.controller

import com.medical.cardio.exception.ApiProblem
import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@Hidden
class ApiErrorController : ErrorController {
    private val log = LoggerFactory.getLogger(javaClass)

    @RequestMapping("/error")
    fun error(request: HttpServletRequest): ProblemDetail {
        val status = (request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int)
            ?.takeIf { it in 400..599 } ?: 500
        if (status >= 500) {
            val cause = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) as? Throwable
            if (cause != null) log.error("Servlet request failed with status {}", status, cause)
        }
        return ApiProblem.forStatus(HttpStatusCode.valueOf(status)).apply {
            val path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as? String
                ?: request.requestURI
            instance = URI.create(path)
        }
    }
}
