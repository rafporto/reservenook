package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantFloorbookEntrySummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toFloorbookSummary
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import org.springframework.stereotype.Service
import java.time.ZoneOffset

@Service
class RestaurantFloorbookService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val restaurantReservationRepository: RestaurantReservationRepository
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<CompanyBackofficeRestaurantFloorbookEntrySummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val parsedDate = RestaurantSupport.parseDate(date, "Date")
        return restaurantReservationRepository.findAllByCompanyIdAndReservedAtBetweenOrderByReservedAtAsc(
            requireNotNull(membership.company.id),
            parsedDate.atStartOfDay().toInstant(ZoneOffset.UTC),
            parsedDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        ).map { it.toFloorbookSummary() }
    }
}
