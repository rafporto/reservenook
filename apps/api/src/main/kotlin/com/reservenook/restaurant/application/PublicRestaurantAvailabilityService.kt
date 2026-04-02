package com.reservenook.restaurant.application

import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import com.reservenook.restaurant.infrastructure.RestaurantServicePeriodRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset

data class PublicRestaurantAvailabilitySummary(
    val startsAt: String,
    val servicePeriodId: Long,
    val servicePeriodName: String,
    val partySize: Int
)

@Service
class PublicRestaurantAvailabilityService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val restaurantServicePeriodRepository: RestaurantServicePeriodRepository,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    private val restaurantReservationRepository: RestaurantReservationRepository,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard
) {
    fun getAvailability(slug: String, date: String, partySize: Int, clientAddress: String): List<PublicRestaurantAvailabilitySummary> {
        publicRequestAbuseGuard.assertClientAllowed("restaurant-availability", clientAddress)
        val company = companyAdminAccessService.requireActivePublicCompany(slug)
        RestaurantSupport.requireRestaurantBusinessType(company.businessType)
        val parsedDate = RestaurantSupport.parseDate(date, "Date")
        val normalizedPartySize = RestaurantSupport.requirePositive(partySize, "Party size")
        val companyId = requireNotNull(company.id)
        val day = RestaurantSupport.businessDayFor(parsedDate)
        val periods = restaurantServicePeriodRepository.findAllByCompanyIdAndDayOfWeekAndActiveTrueOrderByOpensAtAsc(companyId, day)
            .filter {
                normalizedPartySize in it.minPartySize..it.maxPartySize &&
                    parsedDate <= Instant.now().atZone(ZoneOffset.UTC).toLocalDate().plusDays(it.bookingWindowDays.toLong())
            }
        val groups = RestaurantSupport.buildTableGroups(
            restaurantTableRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId),
            restaurantTableCombinationRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId)
        )
        val reservations = restaurantReservationRepository.findAllByCompanyIdAndReservedAtBetweenOrderByReservedAtAsc(
            companyId,
            parsedDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            parsedDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
        return periods.flatMap { period ->
            RestaurantSupport.candidateSlots(parsedDate, period)
                .filter { start ->
                    val end = start.plusSeconds(period.reservationDurationMinutes.toLong() * 60)
                    RestaurantSupport.findAssignableGroup(start, end, normalizedPartySize, reservations, groups) != null
                }
                .map { start ->
                    PublicRestaurantAvailabilitySummary(
                        startsAt = start.toString(),
                        servicePeriodId = requireNotNull(period.id),
                        servicePeriodName = period.name,
                        partySize = normalizedPartySize
                    )
                }
        }
    }
}
