package com.medical.cardio.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val adminUserDetailsService: AdminUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        

        val token = authHeader.substringAfter("Bearer ")

        // Аутентификация
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
        } catch (_: Exception) {
            // Invalid or expired token —  401/403
        }

        filterChain.doFilter(request, response)
    }
}
