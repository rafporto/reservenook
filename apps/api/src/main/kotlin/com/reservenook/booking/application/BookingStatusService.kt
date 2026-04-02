package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.BookingAuditActionType
import com.reservenook.booking.domain.BookingAuditEvent
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BookingStatusService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val bookingRepository: BookingRepository,
    private val bookingAuditEventRepository: BookingAuditEventRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String, status: String?): List<CompanyBackofficeBookingSummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val companyId = requireNotNull(membership.company.id)
        val bookings = if (status.isNullOrBlank()) {
            bookingRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId)
        } else {
            bookingRepository.findAllByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, parseBookingStatus(status))
        }
        return bookings.map { it.toSummary() }
    }

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        bookingId: Long,
        status: String,
        internalNote: String?
    ): CompanyBackofficeBookingSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val booking = bookingRepository.findByIdAndCompanyId(bookingId, requireNotNull(company.id))
            ?: throw IllegalArgumentException("Booking could not be found.")

        val nextStatus = parseBookingStatus(status)
        BookingStatusTransitionPolicy.requireAllowed(booking.status, nextStatus)

        val previousStatus = booking.status
        booking.status = nextStatus
        booking.internalNote = internalNote?.trim()?.ifBlank { null }
        booking.updatedAt = Instant.now()
        bookingRepository.save(booking)

        bookingAuditEventRepository.save(
            BookingAuditEvent(
                booking = booking,
                company = company,
                actionType = BookingAuditActionType.BOOKING_STATUS_UPDATED,
                actorUserId = principal.userId,
                actorEmail = principal.email,
                outcome = SecurityAuditOutcome.SUCCESS,
                details = "${previousStatus.name} -> ${nextStatus.name}"
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_STATUS_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            details = "${previousStatus.name} -> ${nextStatus.name}"
        )
        return booking.toSummary()
    }

    private fun parseBookingStatus(value: String): BookingStatus =
        try {
            BookingStatus.valueOf(value.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Booking status is not supported.")
        }
}
