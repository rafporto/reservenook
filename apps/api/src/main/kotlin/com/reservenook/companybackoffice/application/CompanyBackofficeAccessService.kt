package com.reservenook.companybackoffice.application

import com.reservenook.appointment.infrastructure.AppointmentProviderAvailabilityRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
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
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassInstructorRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.groupclass.infrastructure.ClassTypeRepository
import com.reservenook.restaurant.infrastructure.DiningAreaRepository
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import com.reservenook.restaurant.infrastructure.RestaurantServicePeriodRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.widget.application.WidgetUsageService
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
    private val bookingAuditEventRepository: BookingAuditEventRepository,
    private val appointmentServiceRepository: AppointmentServiceRepository,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val appointmentProviderAvailabilityRepository: AppointmentProviderAvailabilityRepository,
    private val classTypeRepository: ClassTypeRepository,
    private val classInstructorRepository: ClassInstructorRepository,
    private val classSessionRepository: ClassSessionRepository,
    private val classBookingRepository: ClassBookingRepository,
    private val diningAreaRepository: DiningAreaRepository,
    private val restaurantTableRepository: RestaurantTableRepository,
    private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    private val restaurantServicePeriodRepository: RestaurantServicePeriodRepository,
    private val restaurantReservationRepository: RestaurantReservationRepository,
    private val widgetUsageService: WidgetUsageService
) {

    fun getBackoffice(principal: AppAuthenticatedUser, requestedSlug: String): CompanyBackofficeResponse {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val allMemberships = companyMembershipRepository.findAllByCompanyId(companyId)
        val latestSubscription = companySubscriptionRepository.findFirstByCompanyIdOrderByExpiresAtDesc(companyId)
        val providers = appointmentProviderRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId)
        val classSessions = classSessionRepository.findAllByCompanyIdOrderByStartsAtAsc(companyId)

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
            appointmentServices = appointmentServiceRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            appointmentProviders = providers.map { it.toSummary() },
            providerSchedules = providers.map { provider ->
                provider.toScheduleSummary(
                    appointmentProviderAvailabilityRepository.findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(requireNotNull(provider.id))
                )
            },
            classTypes = classTypeRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            classInstructors = classInstructorRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            classSessions = classSessions.map { session ->
                session.toSummary(
                    confirmedCount = classBookingRepository.countByClassSessionIdAndStatusIn(requireNotNull(session.id), listOf(ClassBookingStatus.CONFIRMED, ClassBookingStatus.ATTENDED, ClassBookingStatus.NO_SHOW)).toInt(),
                    waitlistCount = classBookingRepository.countByClassSessionIdAndStatusIn(requireNotNull(session.id), listOf(ClassBookingStatus.WAITLISTED)).toInt()
                )
            },
            classBookings = classBookingRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).map { it.toSummary() },
            diningAreas = diningAreaRepository.findAllByCompanyIdOrderByDisplayOrderAscCreatedAtAsc(companyId).map { it.toSummary() },
            restaurantTables = restaurantTableRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            restaurantTableCombinations = restaurantTableCombinationRepository.findAllByCompanyIdOrderByCreatedAtAsc(companyId).map { it.toSummary() },
            restaurantServicePeriods = restaurantServicePeriodRepository.findAllByCompanyIdOrderByDayOfWeekAscOpensAtAsc(companyId).map { it.toSummary() },
            restaurantReservations = restaurantReservationRepository.findAllByCompanyIdOrderByReservedAtAsc(companyId).map { it.toSummary() },
            staffUsers = allMemberships.sortedBy { it.createdAt }.map { it.toStaffSummary() },
            customerQuestions = companyCustomerQuestionRepository.findAllByCompanyIdOrderByDisplayOrderAsc(companyId).map { it.toSummary() },
            widgetSettings = company.toWidgetSettingsSummary(),
            widgetUsage = widgetUsageService.buildSummary(companyId).let { summary ->
                com.reservenook.companybackoffice.api.CompanyBackofficeWidgetUsageSummary(
                    bootstrapsLast7Days = summary.bootstrapsLast7Days,
                    bookingsLast7Days = summary.bookingsLast7Days,
                    recentOrigins = summary.recentOrigins.map {
                        com.reservenook.companybackoffice.api.CompanyBackofficeWidgetUsageOriginSummary(
                            originHost = it.originHost,
                            bootstrapCount = it.bootstrapCount,
                            bookingCount = it.bookingCount
                        )
                    }
                )
            },
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
                    key = "appointment-services",
                    title = "Appointment services",
                    description = "Define bookable appointment types with duration, buffers, and confirmation behavior.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "appointment-providers",
                    title = "Providers",
                    description = "Manage provider identities, link them to tenant users, and expose only valid tenant-owned schedules.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "provider-availability",
                    title = "Provider availability",
                    description = "Set provider working windows used to generate appointment slots inside tenant scope.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "class-types",
                    title = "Class types",
                    description = "Define reusable class templates with duration, capacity, and confirmation behavior.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "class-instructors",
                    title = "Class instructors",
                    description = "Manage instructors and link them to tenant users without exposing other tenant memberships.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "class-sessions",
                    title = "Class sessions",
                    description = "Schedule dated class sessions with overlap-safe instructor assignment and explicit capacity.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "class-bookings",
                    title = "Class bookings",
                    description = "Review class enrollment, waitlists, and attendance-safe booking outcomes.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "dining-areas",
                    title = "Dining areas",
                    description = "Organize the restaurant floor into tenant-scoped seating areas such as patio, bar, and main hall.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "restaurant-tables",
                    title = "Restaurant tables",
                    description = "Manage restaurant tables with explicit party-size rules and area assignments.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "restaurant-combinations",
                    title = "Combinable tables",
                    description = "Define safe table combinations for larger parties without leaking cross-tenant inventory.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "restaurant-service-periods",
                    title = "Restaurant service periods",
                    description = "Configure reservation windows, slot cadence, and party-size rules for lunch and dinner services.",
                    status = "available"
                ),
                CompanyBackofficeAreaSummary(
                    key = "restaurant-reservations",
                    title = "Restaurant reservations",
                    description = "Review reservation outcomes and keep table assignments consistent for current and upcoming services.",
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
