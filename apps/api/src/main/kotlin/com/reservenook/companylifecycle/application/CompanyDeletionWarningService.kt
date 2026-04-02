package com.reservenook.companylifecycle.application

import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationEvent
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.mail.MailException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class CompanyDeletionWarningService(
    private val companyRepository: CompanyRepository,
    private val inactivityPolicyRepository: InactivityPolicyRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val inactivityNotificationEventRepository: InactivityNotificationEventRepository,
    private val companyDeletionWarningMailSender: CompanyDeletionWarningMailSender,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun warnPendingDeletionCompanies(now: Instant = Instant.now()): CompanyDeletionWarningResult {
        val policy = inactivityPolicyRepository.findById(1L)
            .orElseThrow { IllegalStateException("Missing inactivity policy configuration.") }
        val warningThreshold = now.plus(policy.deletionWarningLeadDays.toLong(), ChronoUnit.DAYS)
        var warningsSent = 0
        var failedWarnings = 0

        val companiesToWarn = companyRepository.findAllByStatus(CompanyStatus.INACTIVE)
            .filter { company ->
                val deletionScheduledAt = company.deletionScheduledAt
                val legalHoldUntil = company.legalHoldUntil
                deletionScheduledAt != null &&
                    !deletionScheduledAt.isAfter(warningThreshold) &&
                    (legalHoldUntil == null || legalHoldUntil.isBefore(now))
            }

        companiesToWarn.forEach { company ->
            val companyId = requireNotNull(company.id)
            val deletionScheduledAt = requireNotNull(company.deletionScheduledAt)
            company.status = CompanyStatus.PENDING_DELETION
            if (
                inactivityNotificationEventRepository.existsByCompanyIdAndNotificationTypeAndStatus(
                    companyId,
                    CompanyLifecycleNotificationType.DELETION_WARNING,
                    InactivityNotificationStatus.SENT
                )
            ) {
                return@forEach
            }

            val recipients = companyMembershipRepository.findAllByCompanyIdAndRole(companyId, CompanyRole.COMPANY_ADMIN)
                .map { membership -> membership.user }
                .filter { user -> user.status == UserStatus.ACTIVE && user.emailVerified }
                .map { user -> user.email }
                .distinct()

            recipients.forEach { email ->
                try {
                    companyDeletionWarningMailSender.sendDeletionWarningEmail(
                        email,
                        company.name,
                        deletionScheduledAt,
                        company.defaultLanguage
                    )
                    inactivityNotificationEventRepository.save(
                        InactivityNotificationEvent(
                            company = company,
                            email = email,
                            notificationType = CompanyLifecycleNotificationType.DELETION_WARNING,
                            status = InactivityNotificationStatus.SENT,
                            notifiedAt = now
                        )
                    )
                    securityAuditService.record(
                        eventType = SecurityAuditEventType.COMPANY_DELETION_WARNING_SENT,
                        outcome = SecurityAuditOutcome.SUCCESS,
                        companySlug = company.slug,
                        targetEmail = email,
                        details = "scheduledDeletionAt=$deletionScheduledAt"
                    )
                    warningsSent += 1
                } catch (exception: MailException) {
                    inactivityNotificationEventRepository.save(
                        InactivityNotificationEvent(
                            company = company,
                            email = email,
                            notificationType = CompanyLifecycleNotificationType.DELETION_WARNING,
                            status = InactivityNotificationStatus.FAILED,
                            failureReason = exception.message,
                            notifiedAt = null
                        )
                    )
                    securityAuditService.record(
                        eventType = SecurityAuditEventType.COMPANY_DELETION_WARNING_FAILED,
                        outcome = SecurityAuditOutcome.FAILURE,
                        companySlug = company.slug,
                        targetEmail = email,
                        details = exception.message
                    )
                    failedWarnings += 1
                }
            }
        }

        return CompanyDeletionWarningResult(
            warningsSent = warningsSent,
            failedWarnings = failedWarnings
        )
    }
}
