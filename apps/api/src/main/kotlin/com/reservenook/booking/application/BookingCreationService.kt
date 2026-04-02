package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingAuditActionType
import com.reservenook.booking.domain.BookingAuditEvent
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class BookingCreationService(
    private val customerContactService: CustomerContactService,
    private val bookingRepository: BookingRepository,
    private val bookingAuditEventRepository: BookingAuditEventRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun findBookingEntity(bookingId: Long): Booking =
        bookingRepository.findById(bookingId).orElseThrow { IllegalArgumentException("Booking could not be found.") }

    fun toSummary(booking: Booking): CompanyBackofficeBookingSummary = booking.toSummary()

    fun create(
        companyId: Long,
        principal: AppAuthenticatedUser?,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        requestSummary: String?,
        preferredDateIso: String?,
        notes: String?,
        source: BookingSource,
        companySlug: String,
        company: com.reservenook.registration.domain.Company
    ): CompanyBackofficeBookingSummary {
        val contact = customerContactService.mergeOrCreate(companyId, company, fullName, email, phone, preferredLanguage, notes)
        val normalizedActorEmail = principal?.email ?: email.trim().lowercase()
        val booking = bookingRepository.save(
            Booking(
                company = company,
                customerContact = contact,
                status = BookingStatus.PENDING,
                source = source,
                requestSummary = requestSummary?.trim()?.ifBlank { null },
                preferredDate = preferredDateIso?.trim()?.ifBlank { null }?.let { LocalDate.parse(it) },
                internalNote = if (source == BookingSource.BACKOFFICE) notes?.trim()?.ifBlank { null } else null
            )
        )
        bookingAuditEventRepository.save(
            BookingAuditEvent(
                booking = booking,
                company = company,
                actionType = BookingAuditActionType.BOOKING_CREATED,
                actorUserId = principal?.userId,
                actorEmail = normalizedActorEmail,
                outcome = SecurityAuditOutcome.SUCCESS,
                details = source.name
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_CREATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal?.userId,
            actorEmail = normalizedActorEmail,
            companySlug = companySlug,
            targetEmail = email.trim().lowercase(),
            details = source.name
        )
        return booking.toSummary()
    }
}
