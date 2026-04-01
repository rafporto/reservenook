package com.reservenook.companybackoffice.application

import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
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
    private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    private val customerContactRepository: CustomerContactRepository,
    private val bookingRepository: BookingRepository,
    private val bookingAuditEventRepository: BookingAuditEventRepository
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
            bookingNotificationTriggers = company.toBookingNotificationTriggersSummary(),
            customerContacts = customerContactRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            bookings = bookingRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).map { it.toSummary() },
            bookingAudit = bookingAuditEventRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).map { it.toSummary() },
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
                    key = "contacts",
                    title = "Customer contacts",
                    description = "Manage tenant-owned customer identities before appointment, class, and restaurant modules are introduced.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "bookings",
                    title = "Booking history",
                    description = "Review shared booking records and status history in one tenant-safe list.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "booking-triggers",
                    title = "Booking notification triggers",
                    description = "Control which booking lifecycle changes send operational notifications.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "booking-audit",
                    title = "Booking audit trail",
                    description = "Inspect who changed booking state and when across the shared booking baseline.",
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
