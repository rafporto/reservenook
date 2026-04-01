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
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SessionCredentialVersionFilter(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val securityAuditService: SecurityAuditService,
    @Value("\${app.security.idle-timeout-seconds:1800}")
    private val idleTimeoutSeconds: Long,
    @Value("\${app.security.absolute-timeout-seconds:43200}")
    private val absoluteTimeoutSeconds: Long
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

        val session = request.getSession(false)
        if (session != null) {
            val nowMillis = System.currentTimeMillis()
            val authenticatedAtMillis =
                (session.getAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS) as? Long) ?: session.creationTime
            val lastSeenAtMillis =
                (session.getAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS) as? Long) ?: authenticatedAtMillis

            if (nowMillis - authenticatedAtMillis > absoluteTimeoutSeconds * 1000) {
                revokeSession(request, response, principal, "ABSOLUTE_TIMEOUT")
                return
            }

            if (nowMillis - lastSeenAtMillis > idleTimeoutSeconds * 1000) {
                revokeSession(request, response, principal, "IDLE_TIMEOUT")
                return
            }

            session.setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, nowMillis)
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
