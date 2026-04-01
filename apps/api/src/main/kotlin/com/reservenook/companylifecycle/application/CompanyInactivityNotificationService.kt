package com.reservenook.companylifecycle.application

import com.reservenook.companylifecycle.domain.InactivityNotificationEvent
import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.mail.MailException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CompanyInactivityNotificationService(
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val inactivityNotificationEventRepository: InactivityNotificationEventRepository,
    private val companyInactivityMailSender: CompanyInactivityMailSender,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun notifyCompanies(companies: List<Company>, now: Instant = Instant.now()): InactivityNotificationResult {
        var notifiedCount = 0
        var failedCount = 0

        companies.forEach { company ->
            val companyId = requireNotNull(company.id)
            if (
                inactivityNotificationEventRepository.existsByCompanyIdAndNotificationTypeAndStatus(
                    companyId,
                    CompanyLifecycleNotificationType.INACTIVITY_NOTICE,
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
                    companyInactivityMailSender.sendInactivityEmail(email, company.name, company.defaultLanguage)
                    inactivityNotificationEventRepository.save(
                        InactivityNotificationEvent(
                            company = company,
                            email = email,
                            notificationType = CompanyLifecycleNotificationType.INACTIVITY_NOTICE,
                            status = InactivityNotificationStatus.SENT,
                            notifiedAt = now
                        )
                    )
                    securityAuditService.record(
                        eventType = SecurityAuditEventType.COMPANY_INACTIVITY_NOTICE_SENT,
                        outcome = SecurityAuditOutcome.SUCCESS,
                        companySlug = company.slug,
                        targetEmail = email
                    )
                    notifiedCount += 1
                } catch (exception: MailException) {
                    inactivityNotificationEventRepository.save(
                        InactivityNotificationEvent(
                            company = company,
                            email = email,
                            notificationType = CompanyLifecycleNotificationType.INACTIVITY_NOTICE,
                            status = InactivityNotificationStatus.FAILED,
                            failureReason = exception.message,
                            notifiedAt = null
                        )
                    )
                    securityAuditService.record(
                        eventType = SecurityAuditEventType.COMPANY_INACTIVITY_NOTICE_FAILED,
                        outcome = SecurityAuditOutcome.FAILURE,
                        companySlug = company.slug,
                        targetEmail = email,
                        details = exception.message
                    )
                    failedCount += 1
                }
            }
        }

        return InactivityNotificationResult(
            companiesNotified = notifiedCount,
            failedNotifications = failedCount
        )
    }
}
