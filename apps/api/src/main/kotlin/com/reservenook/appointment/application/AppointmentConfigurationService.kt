package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentBooking
import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.domain.AppointmentProviderAvailability
import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderAvailabilityRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentProviderSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentServiceSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderScheduleSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.supportedCompanyLanguages
import com.reservenook.companybackoffice.application.toScheduleSummary
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

data class ProviderAvailabilityDraft(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class PublicAppointmentSlotSummary(
    val serviceId: Long,
    val providerId: Long,
    val providerName: String,
    val startsAt: String,
    val endsAt: String
)

data class ProviderScheduleEntrySummary(
    val bookingId: Long,
    val customerName: String,
    val customerEmail: String,
    val status: String,
    val serviceName: String,
    val startsAt: String,
    val endsAt: String
)

@Service
class AppointmentConfigurationService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val appointmentServiceRepository: AppointmentServiceRepository,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val appointmentProviderAvailabilityRepository: AppointmentProviderAvailabilityRepository,
    private val appointmentBookingRepository: AppointmentBookingRepository,
    private val companyBusinessHourRepository: CompanyBusinessHourRepository,
    private val companyClosureDateRepository: CompanyClosureDateRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val companyRepository: CompanyRepository,
    private val userAccountRepository: UserAccountRepository,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun listServices(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeAppointmentServiceSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return appointmentServiceRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun upsertService(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        serviceId: Long?,
        name: String,
        description: String?,
        durationMinutes: Int,
        bufferMinutes: Int,
        priceLabel: String?,
        enabled: Boolean,
        autoConfirm: Boolean
    ): CompanyBackofficeAppointmentServiceSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        if (name.trim().isBlank()) throw IllegalArgumentException("Appointment service name is required.")
        if (durationMinutes !in 15..480) throw IllegalArgumentException("Service duration must be between 15 and 480 minutes.")
        if (bufferMinutes !in 0..180) throw IllegalArgumentException("Service buffer must be between 0 and 180 minutes.")

        val service = serviceId?.let { appointmentServiceRepository.findByIdAndCompanyId(it, requireNotNull(company.id)) }
            ?: AppointmentService(company = company, name = name.trim(), durationMinutes = durationMinutes)

        service.name = name.trim()
        service.description = description?.trim()?.ifBlank { null }
        service.durationMinutes = durationMinutes
        service.bufferMinutes = bufferMinutes
        service.priceLabel = priceLabel?.trim()?.ifBlank { null }
        service.enabled = enabled
        service.autoConfirm = autoConfirm
        service.updatedAt = Instant.now()

        val saved = appointmentServiceRepository.save(service)
        audit(SecurityAuditEventType.APPOINTMENT_SERVICE_UPDATED, principal, company.slug, saved.name)
        return saved.toSummary()
    }

    @Transactional(readOnly = true)
    fun listProviders(principal: AppAuthenticatedUser, requestedSlug: String): Pair<List<CompanyBackofficeAppointmentProviderSummary>, List<CompanyBackofficeProviderScheduleSummary>> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val providers = appointmentProviderRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id))
        return providers.map { it.toSummary() } to providers.map { provider ->
            provider.toScheduleSummary(appointmentProviderAvailabilityRepository.findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(requireNotNull(provider.id)))
        }
    }

    @Transactional
    fun upsertProvider(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long?,
        linkedUserId: Long?,
        displayName: String,
        email: String?,
        active: Boolean
    ): CompanyBackofficeAppointmentProviderSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        if (displayName.trim().isBlank()) throw IllegalArgumentException("Provider display name is required.")
        val linkedMembership = linkedUserId?.let {
            val tenantMembership = companyMembershipRepository.findFirstByUserIdAndCompanySlug(it, requestedSlug)
                ?: throw IllegalArgumentException("Linked provider user must belong to the same company.")
            tenantMembership.user
        }
        if (email != null && email.trim().isNotBlank() && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) {
            throw IllegalArgumentException("Provider email must be a valid email address.")
        }

        val provider = providerId?.let { appointmentProviderRepository.findByIdAndCompanyId(it, requireNotNull(company.id)) }
            ?: AppointmentProvider(company = company, displayName = displayName.trim())
        provider.linkedUser = linkedMembership
        provider.displayName = displayName.trim()
        provider.email = email?.trim()?.ifBlank { null }
        provider.active = active
        provider.updatedAt = Instant.now()

        val saved = appointmentProviderRepository.save(provider)
        audit(SecurityAuditEventType.APPOINTMENT_PROVIDER_UPDATED, principal, company.slug, saved.displayName)
        return saved.toSummary()
    }

    @Transactional
    fun updateProviderAvailability(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long,
        entries: List<ProviderAvailabilityDraft>
    ): CompanyBackofficeProviderScheduleSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val provider = appointmentProviderRepository.findByIdAndCompanyId(providerId, requireNotNull(membership.company.id))
            ?: throw IllegalArgumentException("Provider could not be found.")

        val normalized = entries.map {
            val day = parseDay(it.dayOfWeek)
            val opens = parseTime(it.opensAt, "Opening time must use HH:mm format.")
            val closes = parseTime(it.closesAt, "Closing time must use HH:mm format.")
            if (!opens.isBefore(closes)) throw IllegalArgumentException("Closing time must be after opening time.")
            AppointmentProviderAvailability(
                provider = provider,
                dayOfWeek = day,
                opensAt = opens,
                closesAt = closes,
                displayOrder = it.displayOrder
            )
        }
        normalized.groupBy { it.dayOfWeek }.forEach { (_, perDay) ->
            val sorted = perDay.sortedBy { it.opensAt }
            sorted.zipWithNext().forEach { (current, next) ->
                if (!current.closesAt.isBefore(next.opensAt)) {
                    throw IllegalArgumentException("Provider availability windows cannot overlap on the same day.")
                }
            }
        }
        appointmentProviderAvailabilityRepository.deleteAllByProviderId(providerId)
        val saved = appointmentProviderAvailabilityRepository.saveAll(normalized)
        audit(SecurityAuditEventType.APPOINTMENT_PROVIDER_AVAILABILITY_UPDATED, principal, membership.company.slug, provider.displayName)
        return provider.toScheduleSummary(saved)
    }

    @Transactional(readOnly = true)
    fun getPublicAvailability(slug: String, serviceId: Long, date: String, clientAddress: String): List<PublicAppointmentSlotSummary> {
        publicRequestAbuseGuard.assertClientAllowed("appointment-availability", clientAddress)
        val company = companyAdminAccessServicePublicCompany(slug)
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
            .filter { it.dayOfWeek == parseDay(localDate.dayOfWeek.name) }
        val providers = appointmentProviderRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(company.id)).filter { it.active }
        return providers.flatMap { provider ->
            generateSlotsForProvider(company.id!!, provider, service, localDate, companyHours)
        }.sortedBy { it.startsAt }
    }

    @Transactional
    fun bookPublicAppointment(
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
        val company = companyAdminAccessServicePublicCompany(slug)
        val service = appointmentServiceRepository.findByIdAndCompanyId(serviceId, requireNotNull(company.id))
            ?.takeIf { it.enabled } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")
        val provider = appointmentProviderRepository.findByIdAndCompanyId(providerId, requireNotNull(company.id))
            ?.takeIf { it.active } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")
        val startsAt = Instant.parse(startsAtIso)
        val endAt = startsAt.plus(Duration.ofMinutes((service.durationMinutes + service.bufferMinutes).toLong()))
        validateSlotStillAvailable(company.id!!, provider, service, startsAt, endAt)
        val booking = bookingInfrastructureService.createBookingInternal(
            companyId = company.id!!,
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

    @Transactional
    fun updateAppointmentConfirmation(
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
        audit(SecurityAuditEventType.APPOINTMENT_CONFIRMATION_UPDATED, principal, requestedSlug, normalizedStatus)
        return updated
    }

    @Transactional(readOnly = true)
    fun listProviderSchedule(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<ProviderScheduleEntrySummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val provider = appointmentProviderRepository.findFirstByCompanyIdAndLinkedUserId(requireNotNull(membership.company.id), principal.userId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        val localDate = LocalDate.parse(date)
        val start = localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = localDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return appointmentBookingRepository.findAllByProviderIdAndStartsAtBetweenOrderByStartsAtAsc(requireNotNull(provider.id), start, end).map {
            ProviderScheduleEntrySummary(
                bookingId = requireNotNull(it.booking.id),
                customerName = it.booking.customerContact.fullName,
                customerEmail = it.booking.customerContact.email,
                status = it.booking.status.name,
                serviceName = it.appointmentService.name,
                startsAt = it.startsAt.toString(),
                endsAt = it.endsAt.toString()
            )
        }
    }

    private fun generateSlotsForProvider(
        companyId: Long,
        provider: AppointmentProvider,
        service: AppointmentService,
        date: LocalDate,
        companyHours: List<com.reservenook.companybackoffice.domain.CompanyBusinessHour>
    ): List<PublicAppointmentSlotSummary> {
        val availability = appointmentProviderAvailabilityRepository.findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(requireNotNull(provider.id))
            .filter { it.dayOfWeek == parseDay(date.dayOfWeek.name) }
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
                .map { start ->
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
                    } else null
                }
                .toList()
                .filterNotNull()
        }
    }

    private fun validateSlotStillAvailable(
        companyId: Long,
        provider: AppointmentProvider,
        service: AppointmentService,
        startsAt: Instant,
        endsAt: Instant
    ) {
        val availability = appointmentProviderAvailabilityRepository.findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(provider.id!!)
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
        if (overlapping) throw IllegalArgumentException("The selected appointment slot is no longer available.")
    }

    private fun companyAdminAccessServicePublicCompany(slug: String) =
        companyRepository.findBySlug(slug)
            ?.takeIf { it.status == com.reservenook.registration.domain.CompanyStatus.ACTIVE && it.widgetEnabled }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment booking is unavailable.")

    private fun parseDay(value: String): BusinessDay = try {
        BusinessDay.valueOf(value.trim().uppercase())
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Day of week is not supported.")
    }

    private fun parseTime(value: String, message: String): LocalTime = try {
        LocalTime.parse(value)
    } catch (_: Exception) {
        throw IllegalArgumentException(message)
    }

    private fun audit(
        eventType: SecurityAuditEventType,
        principal: AppAuthenticatedUser? = null,
        companySlug: String,
        details: String?
    ) {
        securityAuditService.record(
            eventType = eventType,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal?.userId,
            actorEmail = principal?.email,
            companySlug = companySlug,
            details = details
        )
    }
}
