package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableCombinationSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.restaurant.domain.RestaurantTableCombination
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

data class TableCombinationDraft(val primaryTableId: Long, val secondaryTableId: Long)

@Service
class TableCombinationManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    private val securityAuditService: SecurityAuditService
) {
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantTableCombinationSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return restaurantTableCombinationRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun replace(principal: AppAuthenticatedUser, requestedSlug: String, entries: List<TableCombinationDraft>): List<CompanyBackofficeRestaurantTableCombinationSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val tables = restaurantTableRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).associateBy { requireNotNull(it.id) }
        val normalizedPairs = entries.map {
            if (it.primaryTableId == it.secondaryTableId) {
                throw IllegalArgumentException("A table cannot be combined with itself.")
            }
            listOf(it.primaryTableId, it.secondaryTableId).sorted()
        }.distinct()

        val combinations = normalizedPairs.map { pair ->
            val primary = tables[pair[0]] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant table could not be found.")
            val secondary = tables[pair[1]] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant table could not be found.")
            RestaurantTableCombination(company = company, primaryTable = primary, secondaryTable = secondary, createdAt = Instant.now())
        }

        restaurantTableCombinationRepository.deleteAllByCompanyId(companyId)
        val saved = restaurantTableCombinationRepository.saveAll(combinations).map { it.toSummary() }
        securityAuditService.record(SecurityAuditEventType.RESTAURANT_TABLE_COMBINATIONS_UPDATED, SecurityAuditOutcome.SUCCESS, principal.userId, principal.email, requestedSlug, details = "pairs=${saved.size}")
        return saved
    }
}
