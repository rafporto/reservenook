package com.reservenook.security.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.infrastructure.UserAccountRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SessionCredentialVersionFilter(
    private val userAccountRepository: UserAccountRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? AppAuthenticatedUser
        if (principal == null) {
            filterChain.doFilter(request, response)
            return
        }

        val user = userAccountRepository.findById(principal.userId).orElse(null)
        if (user == null || user.passwordVersion != principal.passwordVersion) {
            request.getSession(false)?.invalidate()
            SecurityContextHolder.clearContext()
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }

        filterChain.doFilter(request, response)
    }
}
