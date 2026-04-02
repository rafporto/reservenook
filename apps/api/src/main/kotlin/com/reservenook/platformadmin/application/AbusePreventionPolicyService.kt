package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.api.AbusePreventionPolicyResponse
import com.reservenook.platformadmin.domain.AbusePreventionPolicy
import com.reservenook.platformadmin.infrastructure.AbusePreventionPolicyRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class AbusePreventionPolicyService(
    private val abusePreventionPolicyRepository: AbusePreventionPolicyRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun getPolicy(principal: AppAuthenticatedUser): AbusePreventionPolicyResponse {
        requirePlatformAdmin(principal)
        return abusePreventionPolicyRepository.findById(1L)
            .orElseGet(::defaultPolicy)
            .toResponse()
    }

    @Transactional
    fun updatePolicy(
        principal: AppAuthenticatedUser,
        loginPairLimit: Int,
        loginClientLimit: Int,
        loginEmailLimit: Int,
        publicWritePairLimit: Int,
        publicWriteClientLimit: Int,
        publicWriteEmailLimit: Int,
        publicReadClientLimit: Int
    ): AbusePreventionPolicyResponse {
        requirePlatformAdmin(principal)
        listOf(
            "login pair" to loginPairLimit,
            "login client" to loginClientLimit,
            "login email" to loginEmailLimit,
            "public write pair" to publicWritePairLimit,
            "public write client" to publicWriteClientLimit,
            "public write email" to publicWriteEmailLimit,
            "public read client" to publicReadClientLimit
        ).forEach { (label, value) ->
            if (value !in 1..500) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$label limit must be between 1 and 500.")
            }
        }

        val policy = abusePreventionPolicyRepository.findById(1L).orElseGet(::defaultPolicy)
        policy.loginPairLimit = loginPairLimit
        policy.loginClientLimit = loginClientLimit
        policy.loginEmailLimit = loginEmailLimit
        policy.publicWritePairLimit = publicWritePairLimit
        policy.publicWriteClientLimit = publicWriteClientLimit
        policy.publicWriteEmailLimit = publicWriteEmailLimit
        policy.publicReadClientLimit = publicReadClientLimit
        policy.updatedAt = Instant.now()
        abusePreventionPolicyRepository.save(policy)

        securityAuditService.record(
            eventType = SecurityAuditEventType.ABUSE_POLICY_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            details = "loginPair=$loginPairLimit;publicReadClient=$publicReadClientLimit"
        )

        return policy.toResponse()
    }

    @Transactional(readOnly = true)
    fun getSystemPolicy(): AbusePreventionPolicy =
        abusePreventionPolicyRepository.findById(1L).orElseGet(::defaultPolicy)

    private fun defaultPolicy() = AbusePreventionPolicy(
        loginPairLimit = 5,
        loginClientLimit = 10,
        loginEmailLimit = 10,
        publicWritePairLimit = 5,
        publicWriteClientLimit = 10,
        publicWriteEmailLimit = 10,
        publicReadClientLimit = 20
    )

    private fun requirePlatformAdmin(principal: AppAuthenticatedUser) {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
    }

    private fun AbusePreventionPolicy.toResponse() = AbusePreventionPolicyResponse(
        loginPairLimit = loginPairLimit,
        loginClientLimit = loginClientLimit,
        loginEmailLimit = loginEmailLimit,
        publicWritePairLimit = publicWritePairLimit,
        publicWriteClientLimit = publicWriteClientLimit,
        publicWriteEmailLimit = publicWriteEmailLimit,
        publicReadClientLimit = publicReadClientLimit,
        updatedAt = updatedAt.toString()
    )
}
