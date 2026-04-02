package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingNotificationTriggersSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toBookingNotificationTriggersSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingNotificationTriggerService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        destinationEmail: String,
        notifyOnNewBooking: Boolean,
        notifyOnBookingConfirmed: Boolean,
        notifyOnCancellation: Boolean,
        notifyOnBookingCompleted: Boolean,
        notifyOnBookingNoShow: Boolean
    ): CompanyBackofficeBookingNotificationTriggersSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company

        company.notificationDestinationEmail = CommonInputValidation.requireEmail(
            destinationEmail,
            "Booking notification destination must be a valid email address."
        )
        company.notifyOnNewBooking = notifyOnNewBooking
        company.notifyOnBookingConfirmed = notifyOnBookingConfirmed
        company.notifyOnCancellation = notifyOnCancellation
        company.notifyOnBookingCompleted = notifyOnBookingCompleted
        company.notifyOnBookingNoShow = notifyOnBookingNoShow

        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_NOTIFICATION_TRIGGERS_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = company.notificationDestinationEmail
        )
        return company.toBookingNotificationTriggersSummary()
    }
}
