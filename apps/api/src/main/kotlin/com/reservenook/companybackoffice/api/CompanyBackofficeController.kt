package com.reservenook.companybackoffice.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.companybackoffice.application.CompanyBackofficeAccessService
import com.reservenook.companybackoffice.application.BusinessHourDraft
import com.reservenook.companybackoffice.application.ClosureDateDraft
import com.reservenook.companybackoffice.application.CompanyConfigurationService
import com.reservenook.companybackoffice.application.CustomerQuestionDraft
import com.reservenook.companybackoffice.application.CompanyProfileService
import com.reservenook.security.application.RecentAuthenticationGuard
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CompanyBackofficeController(
    private val companyBackofficeAccessService: CompanyBackofficeAccessService,
    private val companyProfileService: CompanyProfileService,
    private val companyConfigurationService: CompanyConfigurationService,
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val recentAuthenticationGuard: RecentAuthenticationGuard
) {

    @GetMapping("/api/app/company/{slug}/backoffice")
    fun backoffice(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ): CompanyBackofficeResponse = companyBackofficeAccessService.getBackoffice(principal, slug)

    @PutMapping("/api/app/company/{slug}/profile")
    fun updateProfile(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyProfileRequest
    ): UpdateCompanyProfileResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        val updatedProfile = companyProfileService.updateProfile(
            principal = principal,
            requestedSlug = slug,
            companyName = request.companyName,
            businessDescription = request.businessDescription,
            contactEmail = request.contactEmail,
            contactPhone = request.contactPhone,
            addressLine1 = request.addressLine1,
            addressLine2 = request.addressLine2,
            city = request.city,
            postalCode = request.postalCode,
            countryCode = request.countryCode
        )

        return UpdateCompanyProfileResponse(
            message = "Company profile updated.",
            company = updatedProfile.company,
            profile = updatedProfile.profile
        )
    }

    @PutMapping("/api/app/company/{slug}/branding")
    fun updateBranding(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyBrandingRequest
    ) : UpdateCompanyBrandingResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyBrandingResponse(
        message = "Company branding updated.",
        branding = companyConfigurationService.updateBranding(
            principal = principal,
            requestedSlug = slug,
            displayName = request.displayName,
            logoUrl = request.logoUrl,
            accentColor = request.accentColor,
            supportEmail = request.supportEmail,
            supportPhone = request.supportPhone
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/localization")
    fun updateLocalization(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyLocalizationRequest
    ): UpdateCompanyLocalizationResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        val localization = companyConfigurationService.updateLocalization(
            principal = principal,
            requestedSlug = slug,
            defaultLanguage = request.defaultLanguage,
            defaultLocale = request.defaultLocale
        )
        val backoffice = companyBackofficeAccessService.getBackoffice(principal, slug)

        return UpdateCompanyLocalizationResponse(
            message = "Language and locale settings updated.",
            localization = localization,
            company = backoffice.company
        )
    }

    @PutMapping("/api/app/company/{slug}/business-hours")
    fun updateBusinessHours(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyBusinessHoursRequest
    ) : UpdateCompanyBusinessHoursResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyBusinessHoursResponse(
        message = "Business hours updated.",
        businessHours = companyConfigurationService.updateBusinessHours(
            principal = principal,
            requestedSlug = slug,
            entries = request.entries.map {
                BusinessHourDraft(
                    dayOfWeek = it.dayOfWeek,
                    opensAt = it.opensAt,
                    closesAt = it.closesAt,
                    displayOrder = it.displayOrder
                )
            }
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/closure-dates")
    fun updateClosureDates(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyClosureDatesRequest
    ) : UpdateCompanyClosureDatesResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyClosureDatesResponse(
        message = "Closure dates updated.",
        closureDates = companyConfigurationService.updateClosureDates(
            principal = principal,
            requestedSlug = slug,
            entries = request.entries.map {
                ClosureDateDraft(
                    label = it.label,
                    startsOn = it.startsOn,
                    endsOn = it.endsOn
                )
            }
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/notification-preferences")
    fun updateNotificationPreferences(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyNotificationPreferencesRequest
    ) : UpdateCompanyNotificationPreferencesResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyNotificationPreferencesResponse(
        message = "Notification preferences updated.",
        notificationPreferences = companyConfigurationService.updateNotificationPreferences(
            principal = principal,
            requestedSlug = slug,
            destinationEmail = request.destinationEmail,
            notifyOnNewBooking = request.notifyOnNewBooking,
            notifyOnCancellation = request.notifyOnCancellation,
            notifyDailySummary = request.notifyDailySummary
        )
    )
    }

    @GetMapping("/api/app/company/{slug}/customer-contacts")
    fun listCustomerContacts(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = CustomerContactsResponse(
        customerContacts = bookingInfrastructureService.listCustomerContacts(principal, slug)
    )

    @PostMapping("/api/app/company/{slug}/customer-contacts")
    fun createCustomerContact(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @Valid @RequestBody request: CreateCustomerContactRequest
    ) = CreateCustomerContactResponse(
        message = "Customer contact saved.",
        customerContact = bookingInfrastructureService.createCustomerContact(
            principal = principal,
            requestedSlug = slug,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            preferredLanguage = request.preferredLanguage,
            notes = request.notes
        )
    )

    @PutMapping("/api/app/company/{slug}/customer-contacts/{contactId}")
    fun updateCustomerContact(
        @PathVariable slug: String,
        @PathVariable contactId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @Valid @RequestBody request: UpdateCustomerContactRequest
    ) = UpdateCustomerContactResponse(
        message = "Customer contact updated.",
        customerContact = bookingInfrastructureService.updateCustomerContact(
            principal = principal,
            requestedSlug = slug,
            contactId = contactId,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            preferredLanguage = request.preferredLanguage,
            notes = request.notes
        )
    )

    @GetMapping("/api/app/company/{slug}/bookings")
    fun listBookings(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @org.springframework.web.bind.annotation.RequestParam(required = false) status: String?
    ) = BookingsResponse(
        bookings = bookingInfrastructureService.listBookings(principal, slug, status)
    )

    @PutMapping("/api/app/company/{slug}/bookings/{bookingId}/status")
    fun updateBookingStatus(
        @PathVariable slug: String,
        @PathVariable bookingId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @Valid @RequestBody request: UpdateBookingStatusRequest
    ) = UpdateBookingStatusResponse(
        message = "Booking status updated.",
        booking = bookingInfrastructureService.updateBookingStatus(
            principal = principal,
            requestedSlug = slug,
            bookingId = bookingId,
            status = request.status,
            internalNote = request.internalNote
        )
    )

    @PutMapping("/api/app/company/{slug}/booking-notification-triggers")
    fun updateBookingNotificationTriggers(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateBookingNotificationTriggersRequest
    ) = UpdateBookingNotificationTriggersResponse(
        message = "Booking notification triggers updated.",
        bookingNotificationTriggers = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            bookingInfrastructureService.updateBookingNotificationTriggers(
                principal = principal,
                requestedSlug = slug,
                destinationEmail = request.destinationEmail,
                notifyOnNewBooking = request.notifyOnNewBooking,
                notifyOnBookingConfirmed = request.notifyOnBookingConfirmed,
                notifyOnCancellation = request.notifyOnCancellation,
                notifyOnBookingCompleted = request.notifyOnBookingCompleted,
                notifyOnBookingNoShow = request.notifyOnBookingNoShow
            )
        }
    )

    @GetMapping("/api/app/company/{slug}/booking-audit")
    fun listBookingAudit(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = BookingAuditResponse(
        bookingAudit = bookingInfrastructureService.listBookingAudit(principal, slug)
    )

    @GetMapping("/api/app/company/{slug}/staff")
    fun listStaffUsers(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = StaffUsersResponse(
        staffUsers = companyConfigurationService.listStaffUsers(principal, slug)
    )

    @PostMapping("/api/app/company/{slug}/staff")
    fun createStaffUser(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: CreateStaffUserRequest
    ) : CreateStaffUserResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return CreateStaffUserResponse(
        message = "Staff user created.",
        staffUser = companyConfigurationService.createStaffUser(
            principal = principal,
            requestedSlug = slug,
            fullName = request.fullName,
            email = request.email,
            role = request.role
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/staff/{membershipId}")
    fun updateStaffUser(
        @PathVariable slug: String,
        @PathVariable membershipId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateStaffUserRequest
    ) : UpdateStaffUserResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateStaffUserResponse(
        message = "Staff user updated.",
        staffUser = companyConfigurationService.updateStaffUser(
            principal = principal,
            requestedSlug = slug,
            membershipId = membershipId,
            role = request.role,
            status = request.status
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/customer-questions")
    fun updateCustomerQuestions(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyCustomerQuestionsRequest
    ) : UpdateCompanyCustomerQuestionsResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyCustomerQuestionsResponse(
        message = "Customer questions updated.",
        customerQuestions = companyConfigurationService.updateCustomerQuestions(
            principal = principal,
            requestedSlug = slug,
            entries = request.entries.map {
                CustomerQuestionDraft(
                    label = it.label,
                    questionType = it.questionType,
                    required = it.required,
                    enabled = it.enabled,
                    displayOrder = it.displayOrder,
                    options = it.options
                )
            }
        )
    )
    }

    @PutMapping("/api/app/company/{slug}/widget-settings")
    fun updateWidgetSettings(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyWidgetSettingsRequest
    ) : UpdateCompanyWidgetSettingsResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateCompanyWidgetSettingsResponse(
        message = "Widget settings updated.",
        widgetSettings = companyConfigurationService.updateWidgetSettings(
            principal = principal,
            requestedSlug = slug,
            ctaLabel = request.ctaLabel,
            widgetEnabled = request.widgetEnabled,
            allowedDomains = request.allowedDomains,
            themeVariant = request.themeVariant
        )
    )
    }
}
