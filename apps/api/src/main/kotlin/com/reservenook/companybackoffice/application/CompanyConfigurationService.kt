package com.reservenook.companybackoffice.application

import org.springframework.beans.factory.annotation.Autowired
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBrandingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBusinessHourSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClosureDateSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerQuestionSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeLocalizationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeNotificationPreferencesSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeStaffUserSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeWidgetSettingsSummary
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyConfigurationService @Autowired constructor(
    private val companyPresentationSettingsService: CompanyPresentationSettingsService,
    private val companyCalendarSettingsService: CompanyCalendarSettingsService,
    private val companyNotificationPreferencesService: CompanyNotificationPreferencesService,
    private val companyStaffManagementService: CompanyStaffManagementService,
    private val companyCustomerQuestionService: CompanyCustomerQuestionService
) {

    constructor(
        companyAdminAccessService: CompanyAdminAccessService,
        companyMembershipRepository: CompanyMembershipRepository,
        userAccountRepository: UserAccountRepository,
        companyBusinessHourRepository: CompanyBusinessHourRepository,
        companyClosureDateRepository: CompanyClosureDateRepository,
        companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
        passwordResetTokenRepository: PasswordResetTokenRepository,
        passwordResetMailSender: PasswordResetMailSender,
        passwordEncoder: PasswordEncoder,
        registrationProperties: RegistrationProperties,
        securityAuditService: SecurityAuditService
    ) : this(
        companyPresentationSettingsService = CompanyPresentationSettingsService(companyAdminAccessService, securityAuditService),
        companyCalendarSettingsService = CompanyCalendarSettingsService(
            companyAdminAccessService,
            companyBusinessHourRepository,
            companyClosureDateRepository,
            securityAuditService
        ),
        companyNotificationPreferencesService = CompanyNotificationPreferencesService(companyAdminAccessService, securityAuditService),
        companyStaffManagementService = CompanyStaffManagementService(
            companyAdminAccessService,
            companyMembershipRepository,
            userAccountRepository,
            passwordResetTokenRepository,
            passwordResetMailSender,
            passwordEncoder,
            registrationProperties,
            securityAuditService
        ),
        companyCustomerQuestionService = CompanyCustomerQuestionService(
            companyAdminAccessService,
            companyCustomerQuestionRepository,
            securityAuditService
        )
    )

    @Transactional
    fun updateBranding(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        displayName: String?,
        logoUrl: String?,
        accentColor: String,
        supportEmail: String,
        supportPhone: String
    ): CompanyBackofficeBrandingSummary =
        companyPresentationSettingsService.updateBranding(principal, requestedSlug, displayName, logoUrl, accentColor, supportEmail, supportPhone)

    @Transactional
    fun updateLocalization(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        defaultLanguage: String,
        defaultLocale: String
    ): CompanyBackofficeLocalizationSummary =
        companyPresentationSettingsService.updateLocalization(principal, requestedSlug, defaultLanguage, defaultLocale)

    @Transactional
    fun updateBusinessHours(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<BusinessHourDraft>
    ): List<CompanyBackofficeBusinessHourSummary> =
        companyCalendarSettingsService.updateBusinessHours(principal, requestedSlug, entries)

    @Transactional
    fun updateClosureDates(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<ClosureDateDraft>
    ): List<CompanyBackofficeClosureDateSummary> =
        companyCalendarSettingsService.updateClosureDates(principal, requestedSlug, entries)

    @Transactional
    fun updateNotificationPreferences(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        destinationEmail: String,
        notifyOnNewBooking: Boolean,
        notifyOnCancellation: Boolean,
        notifyDailySummary: Boolean
    ): CompanyBackofficeNotificationPreferencesSummary =
        companyNotificationPreferencesService.update(
            principal,
            requestedSlug,
            destinationEmail,
            notifyOnNewBooking,
            notifyOnCancellation,
            notifyDailySummary
        )

    @Transactional(readOnly = true)
    fun listStaffUsers(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeStaffUserSummary> =
        companyStaffManagementService.list(principal, requestedSlug)

    @Transactional
    fun createStaffUser(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        role: String
    ): CompanyBackofficeStaffUserSummary =
        companyStaffManagementService.create(principal, requestedSlug, fullName, email, role)

    @Transactional
    fun updateStaffUser(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        membershipId: Long,
        role: String,
        status: String
    ): CompanyBackofficeStaffUserSummary =
        companyStaffManagementService.update(principal, requestedSlug, membershipId, role, status)

    @Transactional
    fun updateCustomerQuestions(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<CustomerQuestionDraft>
    ): List<CompanyBackofficeCustomerQuestionSummary> =
        companyCustomerQuestionService.update(principal, requestedSlug, entries)

    @Transactional
    fun updateWidgetSettings(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        ctaLabel: String?,
        widgetEnabled: Boolean,
        allowedDomains: List<String>,
        themeVariant: String
    ): CompanyBackofficeWidgetSettingsSummary =
        companyPresentationSettingsService.updateWidgetSettings(principal, requestedSlug, ctaLabel, widgetEnabled, allowedDomains, themeVariant)
}

data class BusinessHourDraft(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class ClosureDateDraft(
    val label: String?,
    val startsOn: String,
    val endsOn: String
)

data class CustomerQuestionDraft(
    val label: String,
    val questionType: String,
    val required: Boolean,
    val enabled: Boolean,
    val displayOrder: Int,
    val options: List<String>
)
