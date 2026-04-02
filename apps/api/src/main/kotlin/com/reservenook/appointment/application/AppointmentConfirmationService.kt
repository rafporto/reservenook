package com.reservenook.appointment.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppointmentConfirmationService(
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        bookingId: Long,
        status: String,
        internalNote: String?
    ): CompanyBackofficeBookingSummary {
        val normalizedStatus = status.trim().uppercase()
        if (normalizedStatus !in setOf("CONFIRMED", "CANCELLED")) {
            throw IllegalArgumentException("Appointment confirmation status must be CONFIRMED or CANCELLED.")
        }
        val updated = bookingInfrastructureService.updateBookingStatus(principal, requestedSlug, bookingId, normalizedStatus, internalNote)
        securityAuditService.record(
            eventType = SecurityAuditEventType.APPOINTMENT_CONFIRMATION_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = requestedSlug,
            details = normalizedStatus
        )
        return updated
    }
}
