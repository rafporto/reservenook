package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeAreaSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeOperationsSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeResponse
import com.reservenook.companybackoffice.api.CompanyBackofficeViewerSummary
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import org.springframework.stereotype.Service

@Service
class CompanyBackofficeAccessService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val companySubscriptionRepository: CompanySubscriptionRepository,
    private val companyBusinessHourRepository: CompanyBusinessHourRepository,
    private val companyClosureDateRepository: CompanyClosureDateRepository,
    private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository
) {

    fun getBackoffice(principal: AppAuthenticatedUser, requestedSlug: String): CompanyBackofficeResponse {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val allMemberships = companyMembershipRepository.findAllByCompanyId(companyId)
        val latestSubscription = companySubscriptionRepository.findFirstByCompanyIdOrderByExpiresAtDesc(companyId)

        return CompanyBackofficeResponse(
            company = company.toCompanySummary(),
            profile = company.toProfileSummary(),
            branding = company.toBrandingSummary(),
            localization = company.toLocalizationSummary(),
            businessHours = companyBusinessHourRepository.findAllByCompanyIdOrderByDayOfWeekAscDisplayOrderAsc(companyId).map { it.toSummary() },
            closureDates = companyClosureDateRepository.findAllByCompanyIdOrderByStartsOnAsc(companyId).map { it.toSummary() },
            notificationPreferences = company.toNotificationPreferencesSummary(),
            staffUsers = allMemberships.sortedBy { it.createdAt }.map { it.toStaffSummary() },
            customerQuestions = companyCustomerQuestionRepository.findAllByCompanyIdOrderByDisplayOrderAsc(companyId).map { it.toSummary() },
            widgetSettings = company.toWidgetSettingsSummary(),
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
                    description = "Prepare the brand baseline reused across public booking surfaces and embedded widgets.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "localization",
                    title = "Language and locale",
                    description = "Keep tenant defaults explicit so future customer-facing flows inherit the right language context.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "hours",
                    title = "Business hours",
                    description = "Define weekly operating hours for future availability rules and operational reporting.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "closures",
                    title = "Closure dates",
                    description = "Capture one-off closures and seasonal downtime without breaking tenant scope.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "notifications",
                    title = "Notification preferences",
                    description = "Control which shared operational emails the company wants to receive and where.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "staff",
                    title = "Staff users",
                    description = "Manage tenant users, onboarding, and role coverage without losing admin access.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "questions",
                    title = "Customer questions",
                    description = "Prepare reusable intake questions for later booking flows and customer capture.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "widget",
                    title = "Widget settings",
                    description = "Set the baseline for the embedded booking widget with validated public-facing options.",
                    status = "available"
                )
            )
        )
    }
}
