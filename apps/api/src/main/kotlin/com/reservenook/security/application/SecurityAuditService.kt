package com.reservenook.security.application

import com.reservenook.security.domain.SecurityAuditEvent
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class SecurityAuditService(
    private val securityAuditEventRepository: SecurityAuditEventRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun record(
        eventType: SecurityAuditEventType,
        outcome: SecurityAuditOutcome,
        actorUserId: Long? = null,
        actorEmail: String? = null,
        companySlug: String? = null,
        targetEmail: String? = null,
        details: String? = null
    ) {
        securityAuditEventRepository.save(
            SecurityAuditEvent(
                eventType = eventType,
                outcome = outcome,
                actorUserId = actorUserId,
                actorEmail = actorEmail,
                companySlug = companySlug,
                targetEmail = targetEmail,
                details = details
            )
        )
    }
}
