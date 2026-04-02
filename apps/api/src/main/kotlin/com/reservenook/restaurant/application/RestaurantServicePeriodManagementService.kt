package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantServicePeriodSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.RestaurantServicePeriod
import com.reservenook.restaurant.infrastructure.RestaurantServicePeriodRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class RestaurantServicePeriodManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val restaurantServicePeriodRepository: RestaurantServicePeriodRepository,
    private val securityAuditService: SecurityAuditService
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantServicePeriodSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return restaurantServicePeriodRepository.findAllByCompanyIdOrderByDayOfWeekAscOpensAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        servicePeriodId: Long?,
        name: String,
        dayOfWeek: String,
        opensAt: String,
        closesAt: String,
        slotIntervalMinutes: Int,
        reservationDurationMinutes: Int,
        minPartySize: Int,
        maxPartySize: Int,
        bookingWindowDays: Int,
        active: Boolean
    ): CompanyBackofficeRestaurantServicePeriodSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val now = Instant.now()
        val entity = if (servicePeriodId == null) {
            RestaurantServicePeriod(
                company = company,
                name = RestaurantSupport.requireRestaurantName(name, "Service period name"),
                dayOfWeek = RestaurantSupport.parseDay(dayOfWeek),
                opensAt = RestaurantSupport.parseTime(opensAt, "Opening time"),
                closesAt = RestaurantSupport.parseTime(closesAt, "Closing time"),
                slotIntervalMinutes = slotIntervalMinutes,
                reservationDurationMinutes = reservationDurationMinutes,
                minPartySize = minPartySize,
                maxPartySize = maxPartySize,
                bookingWindowDays = bookingWindowDays,
                active = active,
                createdAt = now,
                updatedAt = now
            )
        } else {
            restaurantServicePeriodRepository.findByIdAndCompanyId(servicePeriodId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Service period could not be found.")
        }
        entity.name = RestaurantSupport.requireRestaurantName(name, "Service period name")
        entity.dayOfWeek = RestaurantSupport.parseDay(dayOfWeek)
        entity.opensAt = RestaurantSupport.parseTime(opensAt, "Opening time")
        entity.closesAt = RestaurantSupport.parseTime(closesAt, "Closing time")
        entity.slotIntervalMinutes = slotIntervalMinutes
        entity.reservationDurationMinutes = reservationDurationMinutes
        entity.minPartySize = minPartySize
        entity.maxPartySize = maxPartySize
        entity.bookingWindowDays = bookingWindowDays
        entity.active = active
        RestaurantSupport.validateServicePeriod(entity)
        entity.updatedAt = now
        val saved = restaurantServicePeriodRepository.save(entity)
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_SERVICE_PERIOD_UPDATED, SecurityAuditOutcome.SUCCESS, principal.userId, principal.email, requestedSlug, details = saved.name)
        return saved.toSummary()
    }
}
