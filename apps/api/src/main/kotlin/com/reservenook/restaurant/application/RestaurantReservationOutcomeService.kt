package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantReservationSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.RestaurantReservationStatus
import com.reservenook.restaurant.domain.RestaurantReservationTable
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import com.reservenook.restaurant.infrastructure.RestaurantReservationTableRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class RestaurantReservationOutcomeService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val restaurantReservationRepository: RestaurantReservationRepository,
    private val restaurantReservationTableRepository: RestaurantReservationTableRepository,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    private val securityAuditService: SecurityAuditService
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantReservationSummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        return restaurantReservationRepository.findAllByCompanyIdOrderByReservedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        reservationId: Long,
        status: String,
        tableIds: List<Long>?
    ): CompanyBackofficeRestaurantReservationSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val companyId = requireNotNull(membership.company.id)
        val reservation = restaurantReservationRepository.findByIdAndCompanyId(reservationId, companyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant reservation could not be found.")
        val nextStatus = try {
            RestaurantReservationStatus.valueOf(status.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Restaurant reservation status is invalid.")
        }

        if (!tableIds.isNullOrEmpty()) {
            val lockedTables = restaurantTableRepository.findAllActiveByCompanyIdForUpdate(companyId)
            val allowedGroups = RestaurantSupport.buildTableGroups(lockedTables, restaurantTableCombinationRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId))
            val normalizedIds = tableIds.sorted()
            val currentIds = reservation.tableAssignments.map { requireNotNull(it.restaurantTable.id) }.sorted()
            val matchingGroup = allowedGroups.firstOrNull { it.tableIds == normalizedIds && reservation.partySize in it.minPartySize..it.maxPartySize }
                ?: throw IllegalArgumentException("Selected table assignment is invalid.")
            if (normalizedIds != currentIds) {
                restaurantReservationTableRepository.deleteAll(reservation.tableAssignments)
                reservation.tableAssignments.clear()
                val byId = lockedTables.associateBy { requireNotNull(it.id) }
                val newAssignments = matchingGroup.tableIds.map { tableId ->
                    RestaurantReservationTable(reservation = reservation, restaurantTable = byId[tableId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant table could not be found."))
                }
                reservation.tableAssignments.addAll(restaurantReservationTableRepository.saveAll(newAssignments))
            }
        }

        reservation.status = nextStatus
        reservation.updatedAt = Instant.now()
        reservation.booking.status = when (nextStatus) {
            RestaurantReservationStatus.CONFIRMED -> BookingStatus.CONFIRMED
            RestaurantReservationStatus.SEATED -> BookingStatus.CONFIRMED
            RestaurantReservationStatus.CANCELLED -> BookingStatus.CANCELLED
            RestaurantReservationStatus.COMPLETED -> BookingStatus.COMPLETED
            RestaurantReservationStatus.NO_SHOW -> BookingStatus.NO_SHOW
        }
        val saved = restaurantReservationRepository.save(reservation)
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_RESERVATION_UPDATED, SecurityAuditOutcome.SUCCESS, principal.userId, principal.email, requestedSlug, details = "${saved.status}:${saved.reservedAt}")
        return saved.toSummary()
    }
}
