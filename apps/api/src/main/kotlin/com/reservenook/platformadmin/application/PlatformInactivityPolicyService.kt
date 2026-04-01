package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.api.InactivityPolicyResponse
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class PlatformInactivityPolicyService(
    private val inactivityPolicyRepository: InactivityPolicyRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun getPolicy(principal: AppAuthenticatedUser): InactivityPolicyResponse {
        requirePlatformAdmin(principal)
        val policy = inactivityPolicyRepository.findById(1L)
            .orElseThrow { IllegalStateException("Missing inactivity policy configuration.") }

        return policy.toResponse()
    }

    @Transactional
    fun updatePolicy(
        principal: AppAuthenticatedUser,
        inactivityThresholdDays: Int,
        deletionWarningLeadDays: Int
    ): InactivityPolicyResponse {
        requirePlatformAdmin(principal)
        validatePolicy(inactivityThresholdDays, deletionWarningLeadDays)

        val policy = inactivityPolicyRepository.findById(1L)
            .orElseThrow { IllegalStateException("Missing inactivity policy configuration.") }

        policy.inactivityThresholdDays = inactivityThresholdDays
        policy.deletionWarningLeadDays = deletionWarningLeadDays
        policy.updatedAt = Instant.now()

        securityAuditService.record(
            eventType = SecurityAuditEventType.PLATFORM_POLICY_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            details = "threshold=$inactivityThresholdDays;lead=$deletionWarningLeadDays"
        )

        return policy.toResponse()
    }

    private fun validatePolicy(inactivityThresholdDays: Int, deletionWarningLeadDays: Int) {
        if (deletionWarningLeadDays > inactivityThresholdDays) {
            throw IllegalArgumentException("Deletion warning lead time cannot be greater than the inactivity threshold.")
        }
    }

    private fun requirePlatformAdmin(principal: AppAuthenticatedUser) {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
    }

    private fun InactivityPolicy.toResponse() = InactivityPolicyResponse(
        inactivityThresholdDays = inactivityThresholdDays,
        deletionWarningLeadDays = deletionWarningLeadDays,
        updatedAt = updatedAt.toString()
    )
}
