package com.reservenook.security.application

import com.reservenook.operations.application.OperationsAlertService
import com.reservenook.security.domain.SecurityAuditEvent
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class SecurityAuditService(
    private val securityAuditEventRepository: SecurityAuditEventRepository,
    private val operationsAlertService: OperationsAlertService
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
        maybeSendOperationalAlert(eventType, outcome, actorEmail, companySlug, targetEmail, details)
    }

    private fun maybeSendOperationalAlert(
        eventType: SecurityAuditEventType,
        outcome: SecurityAuditOutcome,
        actorEmail: String?,
        companySlug: String?,
        targetEmail: String?,
        details: String?
    ) {
        if (outcome == SecurityAuditOutcome.FAILURE && eventType in criticalFailureEvents) {
            operationsAlertService.sendAlert(
                scope = "audit-failure-${eventType.name.lowercase()}",
                title = "${humanize(eventType)} alert",
                intro = "A critical operational failure was recorded by ReserveNook.",
                details = buildDetails(eventType, outcome, actorEmail, companySlug, targetEmail, details)
            )
        }
        if (outcome == SecurityAuditOutcome.RATE_LIMITED && eventType in rateLimitAlertEvents) {
            operationsAlertService.sendAlert(
                scope = "audit-rate-limit-${eventType.name.lowercase()}",
                title = "${humanize(eventType)} spike detected",
                intro = "ReserveNook observed repeated rate-limited traffic on a public or auth-sensitive flow.",
                details = buildDetails(eventType, outcome, actorEmail, companySlug, targetEmail, details)
            )
        }
    }

    private fun buildDetails(
        eventType: SecurityAuditEventType,
        outcome: SecurityAuditOutcome,
        actorEmail: String?,
        companySlug: String?,
        targetEmail: String?,
        details: String?
    ): Map<String, String> = buildMap {
        put("eventType", eventType.name)
        put("outcome", outcome.name)
        actorEmail?.let { put("actorEmail", it) }
        companySlug?.let { put("companySlug", it) }
        targetEmail?.let { put("targetEmail", it) }
        details?.let { put("details", it) }
    }

    private fun humanize(eventType: SecurityAuditEventType): String =
        eventType.name
            .lowercase()
            .split('_')
            .joinToString(" ") { word -> word.replaceFirstChar(Char::titlecase) }

    companion object {
        private val criticalFailureEvents = setOf(
            SecurityAuditEventType.COMPANY_INACTIVITY_NOTICE_FAILED,
            SecurityAuditEventType.COMPANY_DELETION_WARNING_FAILED,
            SecurityAuditEventType.COMPANY_DELETION_FAILED
        )

        private val rateLimitAlertEvents = setOf(
            SecurityAuditEventType.LOGIN_RATE_LIMITED,
            SecurityAuditEventType.PASSWORD_RESET_RATE_LIMITED,
            SecurityAuditEventType.ACTIVATION_RESEND_RATE_LIMITED,
            SecurityAuditEventType.PUBLIC_BOOKING_INTAKE_RATE_LIMITED
        )
    }
}
