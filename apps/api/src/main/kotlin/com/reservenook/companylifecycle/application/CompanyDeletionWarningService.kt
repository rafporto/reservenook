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
    private val companyDeletionWarningMailSender: CompanyDeletionWarningMailSender
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
                deletionScheduledAt != null && !deletionScheduledAt.isAfter(warningThreshold)
            }

        companiesToWarn.forEach { company ->
            val companyId = requireNotNull(company.id)
            val deletionScheduledAt = requireNotNull(company.deletionScheduledAt)
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
                    companyDeletionWarningMailSender.sendDeletionWarningEmail(email, company.name, deletionScheduledAt)
                    inactivityNotificationEventRepository.save(
                        InactivityNotificationEvent(
                            company = company,
                            email = email,
                            notificationType = CompanyLifecycleNotificationType.DELETION_WARNING,
                            status = InactivityNotificationStatus.SENT,
                            notifiedAt = now
                        )
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
