package com.reservenook.companylifecycle.application

import com.reservenook.companylifecycle.domain.InactivityNotificationEvent
import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import org.springframework.mail.MailException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CompanyInactivityNotificationService(
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val inactivityNotificationEventRepository: InactivityNotificationEventRepository,
    private val companyInactivityMailSender: CompanyInactivityMailSender
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
