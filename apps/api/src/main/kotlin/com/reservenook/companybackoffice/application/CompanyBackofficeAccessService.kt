package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeAreaSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCompanySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeOperationsSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeResponse
import com.reservenook.companybackoffice.api.CompanyBackofficeViewerSummary
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CompanyBackofficeAccessService(
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val companySubscriptionRepository: CompanySubscriptionRepository
) {

    fun getBackoffice(principal: AppAuthenticatedUser, requestedSlug: String): CompanyBackofficeResponse {
        if (principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        val membership = companyMembershipRepository.findFirstByUserEmailAndCompanySlug(principal.email, requestedSlug)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")

        if (membership.role != CompanyRole.COMPANY_ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        val companyId = membership.company.id
            ?: throw IllegalStateException("Company identifier is missing.")
        val company = membership.company
        val allMemberships = companyMembershipRepository.findAllByCompanyId(companyId)
        val latestSubscription = companySubscriptionRepository.findFirstByCompanyIdOrderByExpiresAtDesc(companyId)

        return CompanyBackofficeResponse(
            company = CompanyBackofficeCompanySummary(
                companyName = company.name,
                companySlug = company.slug,
                businessType = company.businessType.name,
                companyStatus = company.status.name,
                defaultLanguage = company.defaultLanguage,
                defaultLocale = company.defaultLocale,
                createdAt = company.createdAt.toString()
            ),
            viewer = CompanyBackofficeViewerSummary(
                role = membership.role.name,
                currentUserEmail = principal.email
            ),
            operations = CompanyBackofficeOperationsSummary(
                planType = latestSubscription?.planType?.name ?: "UNKNOWN",
                subscriptionExpiresAt = latestSubscription?.expiresAt?.toString(),
                staffCount = allMemberships.size,
                adminCount = allMemberships.count { it.role == CompanyRole.COMPANY_ADMIN },
                lastActivityAt = company.lastActivityAt.toString(),
                deletionScheduledAt = company.deletionScheduledAt?.toString()
            ),
            configurationAreas = listOf(
                CompanyBackofficeAreaSummary(
                    key = "profile",
                    title = "Company profile",
                    description = "Review and manage the core company identity, business details, and support contacts.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "branding",
                    title = "Branding",
                    description = "Prepare the brand baseline that will later be reused across public booking surfaces.",
                    status = "planned"
                ),
                CompanyBackofficeAreaSummary(
                    key = "localization",
                    title = "Language and locale",
                    description = "Keep tenant defaults explicit so future customer-facing flows inherit the right language context.",
                    status = "planned"
                ),
                CompanyBackofficeAreaSummary(
                    key = "hours",
                    title = "Business hours",
                    description = "Define the weekly operating schedule that future booking availability rules will use.",
                    status = "planned"
                ),
                CompanyBackofficeAreaSummary(
                    key = "staff",
                    title = "Staff users",
                    description = "Manage the people who operate inside this tenant and the roles they hold.",
                    status = "planned"
                ),
                CompanyBackofficeAreaSummary(
                    key = "questions",
                    title = "Customer questions",
                    description = "Prepare reusable intake questions for later booking flows and customer capture.",
                    status = "planned"
                ),
                CompanyBackofficeAreaSummary(
                    key = "widget",
                    title = "Widget settings",
                    description = "Set the baseline for the future embedded booking widget and public entry points.",
                    status = "planned"
                )
            )
        )
    }
}
