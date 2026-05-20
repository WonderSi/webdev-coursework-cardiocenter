package com.medical.cardio.security

import com.medical.cardio.exception.InvalidTokenException
import com.medical.cardio.exception.TokenExpiredException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.cookies
            ?.firstOrNull { it.name == "access_token" }
            ?.value

        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val username = jwtService.extractUsername(token)

            if (username.isNotBlank() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = adminUserDetailsService.loadUserByUsername(username)

                if (jwtService.isTokenValid(token, userDetails)) {
                    val authentication = adminUserDetailsService.authentication(userDetails)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: TokenExpiredException) {
            log.warn("Expired JWT token: ${e.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write("""{"error":"invalid_token"}""")
            return
        } catch (e: InvalidTokenException) {
            log.warn("Invalid JWT token: ${e.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write("""{"error":"invalid_token"}""")
            return
        } catch (e: UsernameNotFoundException) {
            log.warn("User from token not found: ${e.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write("""{"error":"invalid_token"}""")
            return
        }

        filterChain.doFilter(request, response)
    }
}
