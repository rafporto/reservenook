package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.api.PlatformAdminCompanySummaryResponse
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PlatformAdminCompanyListService(
    private val companyRepository: CompanyRepository,
    private val companySubscriptionRepository: CompanySubscriptionRepository
) {

    fun listCompanies(principal: AppAuthenticatedUser): List<PlatformAdminCompanySummaryResponse> {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        val subscriptionsByCompanyId = companySubscriptionRepository.findAll()
            .associateBy { requireNotNull(it.company.id) }

        return companyRepository.findAll()
            .sortedByDescending { it.createdAt }
            .map { company ->
                val subscription = subscriptionsByCompanyId[requireNotNull(company.id)]
                    ?: throw IllegalStateException("Missing subscription for company ${company.slug}.")

                PlatformAdminCompanySummaryResponse(
                    companyName = company.name,
                    companySlug = company.slug,
                    businessType = company.businessType.name,
                    activationStatus = company.status.name,
                    planType = subscription.planType.name,
                    expiresAt = subscription.expiresAt.toString(),
                    legalHoldUntil = company.legalHoldUntil?.toString()
                )
            }
    }
}
