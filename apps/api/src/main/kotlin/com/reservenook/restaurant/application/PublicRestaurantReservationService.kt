package com.reservenook.restaurant.application

import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantReservationSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.RestaurantReservation
import com.reservenook.restaurant.domain.RestaurantReservationStatus
import com.reservenook.restaurant.domain.RestaurantReservationTable
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import com.reservenook.restaurant.infrastructure.RestaurantReservationTableRepository
import com.reservenook.restaurant.infrastructure.RestaurantServicePeriodRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.ZoneOffset

@Service
class PublicRestaurantReservationService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val restaurantServicePeriodRepository: RestaurantServicePeriodRepository,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    private val restaurantReservationRepository: RestaurantReservationRepository,
    private val restaurantReservationTableRepository: RestaurantReservationTableRepository,
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
        partySize: Int,
        startsAt: String
    ): CompanyBackofficeRestaurantReservationSummary {
        val normalizedEmail = email.trim().lowercase()
        publicRequestAbuseGuard.assertAllowed("restaurant-booking", clientAddress, normalizedEmail)
        val company = companyAdminAccessService.requireActivePublicCompany(slug)
        RestaurantSupport.requireRestaurantBusinessType(company.businessType)
        val companyId = requireNotNull(company.id)
        val reservationStart = RestaurantSupport.parseInstant(startsAt, "Reservation start")
        val normalizedPartySize = RestaurantSupport.requirePositive(partySize, "Party size")
        val reservationDate = reservationStart.atZone(ZoneOffset.UTC).toLocalDate()
        val servicePeriod = restaurantServicePeriodRepository.findAllByCompanyIdAndDayOfWeekAndActiveTrueOrderByOpensAtAsc(
            companyId,
            RestaurantSupport.businessDayFor(reservationDate)
        ).firstOrNull {
            val localTime = reservationStart.atZone(ZoneOffset.UTC).toLocalTime()
            localTime >= it.opensAt &&
                localTime.plusMinutes(it.reservationDurationMinutes.toLong()) <= it.closesAt &&
                normalizedPartySize in it.minPartySize..it.maxPartySize
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant reservation is unavailable.")

        val lockedTables = restaurantTableRepository.findAllActiveByCompanyIdForUpdate(companyId)
        val groups = RestaurantSupport.buildTableGroups(lockedTables, restaurantTableCombinationRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId))
        val reservations = restaurantReservationRepository.findAllByCompanyIdAndReservedAtBetweenOrderByReservedAtAsc(
            companyId,
            reservationDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            reservationDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
        val reservationEnd = reservationStart.plusSeconds(servicePeriod.reservationDurationMinutes.toLong() * 60)
        val group = RestaurantSupport.findAssignableGroup(reservationStart, reservationEnd, normalizedPartySize, reservations, groups)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "No tables are available for the selected time.")

        val booking = bookingInfrastructureService.createBookingInternal(
            companyId = companyId,
            principal = null,
            fullName = fullName,
            email = normalizedEmail,
            phone = phone,
            preferredLanguage = preferredLanguage ?: company.defaultLanguage,
            requestSummary = "Restaurant reservation for $normalizedPartySize guests",
            preferredDateIso = reservationDate.toString(),
            notes = null,
            source = BookingSource.PUBLIC_WEB,
            companySlug = company.slug,
            company = company
        )
        val bookingEntity = bookingInfrastructureService.findBookingEntity(requireNotNull(booking.id))
        bookingEntity.status = BookingStatus.CONFIRMED
        val reservation = restaurantReservationRepository.save(
            RestaurantReservation(
                booking = bookingEntity,
                company = company,
                servicePeriod = servicePeriod,
                reservedAt = reservationStart,
                reservedUntil = reservationEnd,
                partySize = normalizedPartySize,
                status = RestaurantReservationStatus.CONFIRMED
            )
        )
        val tablesById = lockedTables.associateBy { requireNotNull(it.id) }
        val assignments = restaurantReservationTableRepository.saveAll(group.tableIds.map { tableId ->
            RestaurantReservationTable(
                reservation = reservation,
                restaurantTable = tablesById[tableId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant table could not be found.")
            )
        })
        reservation.tableAssignments.addAll(assignments)
        val saved = restaurantReservationRepository.findByIdAndCompanyId(requireNotNull(reservation.id), companyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant reservation could not be found.")
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_BOOKED, SecurityAuditOutcome.SUCCESS, actorEmail = normalizedEmail, companySlug = company.slug, targetEmail = normalizedEmail, details = reservationStart.toString())
        return saved.toSummary()
    }
}
