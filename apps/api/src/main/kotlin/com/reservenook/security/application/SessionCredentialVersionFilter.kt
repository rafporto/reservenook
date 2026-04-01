package com.reservenook.security.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SessionCredentialVersionFilter(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val securityAuditService: SecurityAuditService
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
            revokeSession(request, response, principal, "PASSWORD_VERSION")
            return
        }

        if (!principal.isPlatformAdmin) {
            val membership = companyMembershipRepository.findFirstByUserIdAndCompanySlug(principal.userId, principal.companySlug.orEmpty())
            if (membership == null || membership.company.status != CompanyStatus.ACTIVE) {
                revokeSession(request, response, principal, "COMPANY_STATUS")
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun revokeSession(
        request: HttpServletRequest,
        response: HttpServletResponse,
        principal: AppAuthenticatedUser,
        reason: String
    ) {
        request.getSession(false)?.invalidate()
        SecurityContextHolder.clearContext()
        securityAuditService.record(
            eventType = SecurityAuditEventType.SESSION_REVOKED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = principal.companySlug,
            details = reason
        )
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
    }
}
