package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeNotificationPreferencesSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyNotificationPreferencesService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        destinationEmail: String,
        notifyOnNewBooking: Boolean,
        notifyOnCancellation: Boolean,
        notifyDailySummary: Boolean
    ): CompanyBackofficeNotificationPreferencesSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company

        company.notificationDestinationEmail = CommonInputValidation.requireEmail(
            destinationEmail,
            "Notification destination must be a valid email address."
        )
        company.notifyOnNewBooking = notifyOnNewBooking
        company.notifyOnCancellation = notifyOnCancellation
        company.notifyDailySummary = notifyDailySummary

        securityAuditService.record(
            eventType = SecurityAuditEventType.COMPANY_NOTIFICATION_PREFERENCES_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug
        )
        return company.toNotificationPreferencesSummary()
    }
}
