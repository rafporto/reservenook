package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentBooking
import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.application.supportedCompanyLanguages
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZoneOffset

@Service
class PublicAppointmentBookingService(
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val appointmentBookingRepository: AppointmentBookingRepository,
    private val appointmentSlotGenerationService: AppointmentSlotGenerationService,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun book(
        slug: String,
        clientAddress: String,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        serviceId: Long,
        providerId: Long,
        startsAtIso: String
    ): CompanyBackofficeBookingSummary {
        val normalizedEmail = email.trim().lowercase()
        publicRequestAbuseGuard.assertAllowed("appointment-booking", clientAddress, normalizedEmail)
        val (company, service, provider) = appointmentSlotGenerationService.validatePublicSlotAvailability(slug, serviceId, providerId, startsAtIso)
        val startsAt = java.time.Instant.parse(startsAtIso)
        val booking = bookingInfrastructureService.createBookingInternal(
            companyId = requireNotNull(company.id),
            principal = null,
            fullName = fullName,
            email = normalizedEmail,
            phone = phone,
            preferredLanguage = preferredLanguage?.takeIf { it in supportedCompanyLanguages() } ?: company.defaultLanguage,
            requestSummary = "${service.name} with ${provider.displayName}",
            preferredDateIso = startsAt.atOffset(ZoneOffset.UTC).toLocalDate().toString(),
            notes = null,
            source = BookingSource.PUBLIC_WEB,
            companySlug = company.slug,
            company = company
        )
        val savedBooking = bookingInfrastructureService.findBookingEntity(requireNotNull(booking.id))
        savedBooking.status = if (service.autoConfirm) BookingStatus.CONFIRMED else BookingStatus.PENDING
        appointmentBookingRepository.save(
            AppointmentBooking(
                booking = savedBooking,
                company = company,
                appointmentService = service,
                provider = provider,
                startsAt = startsAt,
                endsAt = startsAt.plus(Duration.ofMinutes(service.durationMinutes.toLong()))
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.APPOINTMENT_BOOKED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorEmail = normalizedEmail,
            companySlug = slug,
            details = "${service.name}:${provider.displayName}"
        )
        return bookingInfrastructureService.toSummary(savedBooking)
    }
}
