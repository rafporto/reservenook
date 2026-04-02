package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.domain.CompanyBusinessHour
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class AppointmentSlotGenerationService(
    private val companyRepository: CompanyRepository,
    private val appointmentServiceRepository: AppointmentServiceRepository,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val providerAvailabilityService: ProviderAvailabilityService,
    private val appointmentBookingRepository: AppointmentBookingRepository,
    private val companyBusinessHourRepository: CompanyBusinessHourRepository,
    private val companyClosureDateRepository: CompanyClosureDateRepository,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard
) {

    @Transactional(readOnly = true)
    fun getPublicAvailability(slug: String, serviceId: Long, date: String, clientAddress: String): List<PublicAppointmentSlotSummary> {
        publicRequestAbuseGuard.assertClientAllowed("appointment-availability", clientAddress)
        val company = findActivePublicCompany(slug)
        val localDate = LocalDate.parse(date)
        if (localDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
            throw IllegalArgumentException("Availability date must be today or later.")
        }
        if (localDate.isAfter(LocalDate.now(ZoneOffset.UTC).plusDays(60))) {
            throw IllegalArgumentException("Availability can only be queried up to 60 days ahead.")
        }
        val service = appointmentServiceRepository.findByIdAndCompanyId(serviceId, requireNotNull(company.id))
            ?.takeIf { it.enabled }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment availability is unavailable.")
        if (companyClosureDateRepository.findAllByCompanyIdOrderByStartsOnAsc(requireNotNull(company.id)).any { !localDate.isBefore(it.startsOn) && !localDate.isAfter(it.endsOn) }) {
            return emptyList()
        }
        val companyHours = companyBusinessHourRepository.findAllByCompanyIdOrderByDayOfWeekAscDisplayOrderAsc(requireNotNull(company.id))
            .filter { it.dayOfWeek == AppointmentSupport.parseDay(localDate.dayOfWeek.name) }
        val providers = appointmentProviderRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(company.id)).filter { it.active }
        return providers.flatMap { provider ->
            generateSlotsForProvider(provider, service, localDate, companyHours)
        }.sortedBy { it.startsAt }
    }

    @Transactional(readOnly = true)
    fun validatePublicSlotAvailability(
        slug: String,
        serviceId: Long,
        providerId: Long,
        startsAtIso: String
    ): Triple<Company, AppointmentService, AppointmentProvider> {
        val company = findActivePublicCompany(slug)
        val service = appointmentServiceRepository.findByIdAndCompanyId(serviceId, requireNotNull(company.id))
            ?.takeIf { it.enabled } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")
        val provider = appointmentProviderRepository.findByIdAndCompanyId(providerId, requireNotNull(company.id))
            ?.takeIf { it.active } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")
        val startsAt = Instant.parse(startsAtIso)
        val endsAt = startsAt.plus(Duration.ofMinutes((service.durationMinutes + service.bufferMinutes).toLong()))
        validateSlotStillAvailable(provider, service, startsAt, endsAt)
        return Triple(company, service, provider)
    }

    private fun generateSlotsForProvider(
        provider: AppointmentProvider,
        service: AppointmentService,
        date: LocalDate,
        companyHours: List<CompanyBusinessHour>
    ): List<PublicAppointmentSlotSummary> {
        val availability = providerAvailabilityService.listForProvider(requireNotNull(provider.id))
            .filter { it.dayOfWeek == AppointmentSupport.parseDay(date.dayOfWeek.name) }
        val existing = appointmentBookingRepository.findAllByProviderIdAndStartsAtBetweenOrderByStartsAtAsc(
            provider.id!!,
            date.atStartOfDay().toInstant(ZoneOffset.UTC),
            date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        ).filter { it.booking.status in setOf(BookingStatus.PENDING, BookingStatus.CONFIRMED) }

        val durationMinutes = service.durationMinutes.toLong()
        return availability.flatMap { rule ->
            val companyOpen = companyHours.maxOfOrNull { if (it.opensAt.isAfter(rule.opensAt)) it.opensAt else rule.opensAt } ?: rule.opensAt
            val companyClose = companyHours.minOfOrNull { if (it.closesAt.isBefore(rule.closesAt)) it.closesAt else rule.closesAt } ?: rule.closesAt
            val closeCutoff = companyClose.minusMinutes(durationMinutes)
            generateSequence(date.atTime(companyOpen)) { it.plusMinutes(durationMinutes) }
                .takeWhile { !it.toLocalTime().isAfter(closeCutoff) }
                .mapNotNull { start ->
                    val end = start.plusMinutes(durationMinutes)
                    val startInstant = start.toInstant(ZoneOffset.UTC)
                    val endInstant = end.toInstant(ZoneOffset.UTC)
                    if (existing.none { startInstant < it.endsAt && endInstant > it.startsAt }) {
                        PublicAppointmentSlotSummary(
                            serviceId = requireNotNull(service.id),
                            providerId = requireNotNull(provider.id),
                            providerName = provider.displayName,
                            startsAt = startInstant.toString(),
                            endsAt = endInstant.toString()
                        )
                    } else {
                        null
                    }
                }.toList()
        }
    }

    private fun validateSlotStillAvailable(
        provider: AppointmentProvider,
        service: AppointmentService,
        startsAt: Instant,
        endsAt: Instant
    ) {
        val availability = providerAvailabilityService.listForProvider(requireNotNull(provider.id))
            .filter { it.dayOfWeek.name == startsAt.atOffset(ZoneOffset.UTC).dayOfWeek.name }
        val startsLocalTime = startsAt.atOffset(ZoneOffset.UTC).toLocalTime()
        val endsLocalTime = startsAt.plus(Duration.ofMinutes(service.durationMinutes.toLong())).atOffset(ZoneOffset.UTC).toLocalTime()
        if (availability.none { !startsLocalTime.isBefore(it.opensAt) && !endsLocalTime.isAfter(it.closesAt) }) {
            throw IllegalArgumentException("The selected appointment slot is no longer available.")
        }
        val overlapping = appointmentBookingRepository.findAllByProviderIdAndStartsAtBetweenOrderByStartsAtAsc(
            provider.id!!,
            startsAt.minus(Duration.ofHours(8)),
            endsAt.plus(Duration.ofHours(8))
        ).any {
            it.booking.status in setOf(BookingStatus.PENDING, BookingStatus.CONFIRMED) &&
                startsAt < it.endsAt && endsAt > it.startsAt
        }
        if (overlapping) {
            throw IllegalArgumentException("The selected appointment slot is no longer available.")
        }
    }

    fun findActivePublicCompany(slug: String): Company =
        companyRepository.findBySlug(slug)
            ?.takeIf { it.status == CompanyStatus.ACTIVE && it.widgetEnabled }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")
}
