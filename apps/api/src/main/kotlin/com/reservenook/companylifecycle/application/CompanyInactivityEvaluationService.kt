package com.reservenook.companylifecycle.application

import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class CompanyInactivityEvaluationService(
    private val companyRepository: CompanyRepository,
    private val inactivityPolicyRepository: InactivityPolicyRepository,
    private val companyInactivityNotificationService: CompanyInactivityNotificationService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun evaluate(now: Instant = Instant.now()): InactivityEvaluationResult {
        val policy = inactivityPolicyRepository.findById(1L)
            .orElseThrow { IllegalStateException("Missing inactivity policy configuration.") }
        val cutoff = now.minus(policy.inactivityThresholdDays.toLong(), ChronoUnit.DAYS)

        val companiesToMarkInactive = companyRepository.findAllByStatus(CompanyStatus.ACTIVE)
            .filter { company -> !company.lastActivityAt.isAfter(cutoff) }

        companiesToMarkInactive.forEach { company ->
            company.status = CompanyStatus.INACTIVE
            company.inactiveAt = now
            company.deletionScheduledAt = now.plus(policy.inactivityThresholdDays.toLong(), ChronoUnit.DAYS)
            securityAuditService.record(
                eventType = SecurityAuditEventType.COMPANY_MARKED_INACTIVE,
                outcome = SecurityAuditOutcome.SUCCESS,
                companySlug = company.slug,
                details = "scheduledDeletionAt=${company.deletionScheduledAt}"
            )
        }

        companyInactivityNotificationService.notifyCompanies(companiesToMarkInactive, now)

        return InactivityEvaluationResult(companiesMarkedInactive = companiesToMarkInactive.size)
    }
}
