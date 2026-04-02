package com.reservenook.security.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.operations.application.OperationsAlertService
import com.reservenook.security.domain.SecurityAuditEvent
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant

data class SecurityAuditRecordSummary(
    val id: Long,
    val eventType: String,
    val outcome: String,
    val actorEmail: String?,
    val companySlug: String?,
    val targetEmail: String?,
    val details: String?,
    val createdAt: String
)

data class SecurityOperationsSummary(
    val auditEventsLast24Hours: Int,
    val rateLimitedEventsLast24Hours: Int,
    val loginFailuresLast24Hours: Int,
    val bookingEventsLast24Hours: Int,
    val lifecycleEventsLast24Hours: Int,
    val alertingEnabled: Boolean,
    val alertRecipient: String?,
    val latestCriticalEvents: List<SecurityAuditRecordSummary>
)

@Service
class SecurityAuditQueryService(
    private val securityAuditEventRepository: SecurityAuditEventRepository,
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val operationsAlertService: OperationsAlertService
) {

    fun listPlatformAudit(principal: AppAuthenticatedUser): List<SecurityAuditRecordSummary> {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
        return securityAuditEventRepository.findTop100ByOrderByCreatedAtDesc().map { it.toSummary() }
    }

    fun listCompanyAudit(principal: AppAuthenticatedUser, requestedSlug: String): List<SecurityAuditRecordSummary> {
        companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return securityAuditEventRepository.findTop100ByCompanySlugOrderByCreatedAtDesc(requestedSlug).map { it.toSummary() }
    }

    fun getPlatformSummary(principal: AppAuthenticatedUser, now: Instant = Instant.now()): SecurityOperationsSummary {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
        val events = securityAuditEventRepository.findAllByCreatedAtAfter(now.minus(Duration.ofHours(24)))
        return buildSummary(events)
    }

    fun getCompanySummary(principal: AppAuthenticatedUser, requestedSlug: String, now: Instant = Instant.now()): SecurityOperationsSummary {
        companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val events = securityAuditEventRepository.findAllByCompanySlugAndCreatedAtAfter(requestedSlug, now.minus(Duration.ofHours(24)))
        return buildSummary(events)
    }

    private fun buildSummary(events: List<SecurityAuditEvent>) = SecurityOperationsSummary(
        auditEventsLast24Hours = events.size,
        rateLimitedEventsLast24Hours = events.count { it.outcome.name == "RATE_LIMITED" },
        loginFailuresLast24Hours = events.count { it.eventType.name == "LOGIN_FAILURE" },
        bookingEventsLast24Hours = events.count {
            it.eventType.name in setOf("BOOKING_CREATED", "APPOINTMENT_BOOKED", "CLASS_BOOKED", "RESTAURANT_BOOKED")
        },
        lifecycleEventsLast24Hours = events.count {
            it.eventType.name.startsWith("COMPANY_")
        },
        alertingEnabled = operationsAlertService.isAlertingEnabled(),
        alertRecipient = operationsAlertService.configuredRecipient(),
        latestCriticalEvents = events
            .sortedByDescending { it.createdAt }
            .take(10)
            .map { it.toSummary() }
    )

    private fun SecurityAuditEvent.toSummary() = SecurityAuditRecordSummary(
        id = requireNotNull(id),
        eventType = eventType.name,
        outcome = outcome.name,
        actorEmail = actorEmail,
        companySlug = companySlug,
        targetEmail = targetEmail,
        details = details,
        createdAt = createdAt.toString()
    )
}
