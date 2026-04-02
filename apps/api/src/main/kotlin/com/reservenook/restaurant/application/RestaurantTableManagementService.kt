package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.RestaurantTable
import com.reservenook.restaurant.infrastructure.DiningAreaRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class RestaurantTableManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val diningAreaRepository: DiningAreaRepository,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val securityAuditService: SecurityAuditService
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantTableSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return restaurantTableRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        tableId: Long?,
        diningAreaId: Long,
        label: String,
        minPartySize: Int,
        maxPartySize: Int,
        active: Boolean
    ): CompanyBackofficeRestaurantTableSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val now = Instant.now()
        val area = diningAreaRepository.findByIdAndCompanyId(diningAreaId, companyId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dining area could not be found.")
        val (minParty, maxParty) = RestaurantSupport.requireCapacityRange(minPartySize, maxPartySize)
        val entity = if (tableId == null) {
            RestaurantTable(company = company, diningArea = area, label = RestaurantSupport.requireRestaurantName(label, "Table label"), minPartySize = minParty, maxPartySize = maxParty, active = active, createdAt = now, updatedAt = now)
        } else {
            restaurantTableRepository.findByIdAndCompanyId(tableId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant table could not be found.")
        }
        entity.diningArea = area
        entity.label = RestaurantSupport.requireRestaurantName(label, "Table label")
        entity.minPartySize = minParty
        entity.maxPartySize = maxParty
        entity.active = active
        entity.updatedAt = now
        val saved = restaurantTableRepository.save(entity)
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_TABLE_UPDATED, SecurityAuditOutcome.SUCCESS, principal.userId, principal.email, requestedSlug, details = saved.label)
        return saved.toSummary()
    }
}
