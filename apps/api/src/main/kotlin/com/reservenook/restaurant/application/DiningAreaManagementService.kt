package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeDiningAreaSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.DiningArea
import com.reservenook.restaurant.infrastructure.DiningAreaRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class DiningAreaManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val diningAreaRepository: DiningAreaRepository,
    private val securityAuditService: SecurityAuditService
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeDiningAreaSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return diningAreaRepository.findAllByCompanyIdOrderByDisplayOrderAscCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        diningAreaId: Long?,
        name: String,
        displayOrder: Int,
        active: Boolean
    ): CompanyBackofficeDiningAreaSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val now = Instant.now()
        val entity = if (diningAreaId == null) {
            DiningArea(company = company, name = RestaurantSupport.requireRestaurantName(name, "Dining area name"), displayOrder = displayOrder, active = active, createdAt = now, updatedAt = now)
        } else {
            diningAreaRepository.findByIdAndCompanyId(diningAreaId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Dining area could not be found.")
        }
        entity.name = RestaurantSupport.requireRestaurantName(name, "Dining area name")
        entity.displayOrder = displayOrder
        entity.active = active
        entity.updatedAt = now
        val saved = diningAreaRepository.save(entity)
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_DINING_AREA_UPDATED, SecurityAuditOutcome.SUCCESS, principal.userId, principal.email, requestedSlug, details = saved.name)
        return saved.toSummary()
    }
}
