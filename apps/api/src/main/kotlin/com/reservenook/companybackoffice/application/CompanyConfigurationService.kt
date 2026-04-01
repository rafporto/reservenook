package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBrandingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBusinessHourSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClosureDateSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerQuestionSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeLocalizationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeNotificationPreferencesSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeStaffUserSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeWidgetSettingsSummary
import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.companybackoffice.domain.CompanyBusinessHour
import com.reservenook.companybackoffice.domain.CompanyClosureDate
import com.reservenook.companybackoffice.domain.CompanyCustomerQuestion
import com.reservenook.companybackoffice.domain.CustomerQuestionType
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CompanyConfigurationService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val userAccountRepository: UserAccountRepository,
    private val companyBusinessHourRepository: CompanyBusinessHourRepository,
    private val companyClosureDateRepository: CompanyClosureDateRepository,
    private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordResetMailSender: PasswordResetMailSender,
    private val passwordEncoder: PasswordEncoder,
    private val registrationProperties: RegistrationProperties,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun updateBranding(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        displayName: String?,
        logoUrl: String?,
        accentColor: String,
        supportEmail: String,
        supportPhone: String
    ): CompanyBackofficeBrandingSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company

        if (!Regex("^#[0-9A-Fa-f]{6}$").matches(accentColor.trim())) {
            throw IllegalArgumentException("Accent color must use the #RRGGBB format.")
        }
        if (displayName?.trim()?.length ?: 0 > 255) {
            throw IllegalArgumentException("Display name must be 255 characters or fewer.")
        }
        if (logoUrl != null && logoUrl.trim().isNotBlank()) {
            try {
                val parsed = URI(logoUrl.trim())
                if (parsed.scheme !in setOf("https", "http")) {
                    throw IllegalArgumentException("Logo URL must use http or https.")
                }
            } catch (_: Exception) {
                throw IllegalArgumentException("Logo URL must be a valid absolute URL.")
            }
        }
        validateEmail(supportEmail, "Support email must be a valid email address.")
        validatePhone(supportPhone, "Support phone must be a valid phone number.")

        company.brandDisplayName = displayName?.trim()?.ifBlank { null }
        company.brandLogoUrl = logoUrl?.trim()?.ifBlank { null }
        company.brandAccentColor = accentColor.trim().uppercase()
        company.supportEmail = supportEmail.trim().lowercase()
        company.supportPhone = supportPhone.trim()

        audit(SecurityAuditEventType.COMPANY_BRANDING_UPDATED, principal, company.slug)
        return company.toBrandingSummary()
    }

    @Transactional
    fun updateLocalization(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        defaultLanguage: String,
        defaultLocale: String
    ): CompanyBackofficeLocalizationSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val normalizedLanguage = defaultLanguage.trim().lowercase()
        val normalizedLocale = defaultLocale.trim()

        if (normalizedLanguage !in supportedCompanyLanguages()) {
            throw IllegalArgumentException("Default language is not supported.")
        }
        if (normalizedLocale !in supportedCompanyLocales()) {
            throw IllegalArgumentException("Default locale is not supported.")
        }

        val allowedLocales = when (normalizedLanguage) {
            "en" -> setOf("en-US", "en-GB")
            "de" -> setOf("de-DE")
            "pt" -> setOf("pt-PT", "pt-BR")
            else -> emptySet()
        }
        if (normalizedLocale !in allowedLocales) {
            throw IllegalArgumentException("Default locale is not compatible with the selected language.")
        }

        company.defaultLanguage = normalizedLanguage
        company.defaultLocale = normalizedLocale

        audit(SecurityAuditEventType.COMPANY_LOCALIZATION_UPDATED, principal, company.slug)
        return company.toLocalizationSummary()
    }

    @Transactional
    fun updateBusinessHours(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<BusinessHourDraft>
    ): List<CompanyBackofficeBusinessHourSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map { entry ->
            val dayOfWeek = parseDay(entry.dayOfWeek)
            val opensAt = parseTime(entry.opensAt, "Opening time must use HH:mm format.")
            val closesAt = parseTime(entry.closesAt, "Closing time must use HH:mm format.")
            if (!opensAt.isBefore(closesAt)) {
                throw IllegalArgumentException("Closing time must be after opening time.")
            }
            NormalizedBusinessHourDraft(dayOfWeek, opensAt, closesAt, entry.displayOrder)
        }

        normalizedEntries
            .groupBy { it.dayOfWeek }
            .forEach { (_, dayEntries) ->
                val sorted = dayEntries.sortedBy { it.opensAt }
                sorted.zipWithNext().forEach { (current, next) ->
                    if (!current.closesAt.isBefore(next.opensAt)) {
                        throw IllegalArgumentException("Business hour windows cannot overlap on the same day.")
                    }
                }
            }

        companyBusinessHourRepository.deleteAllByCompanyId(companyId)
        val saved = companyBusinessHourRepository.saveAll(
            normalizedEntries.map {
                CompanyBusinessHour(
                    company = company,
                    dayOfWeek = it.dayOfWeek,
                    opensAt = it.opensAt,
                    closesAt = it.closesAt,
                    displayOrder = it.displayOrder
                )
            }
        )

        audit(SecurityAuditEventType.COMPANY_BUSINESS_HOURS_UPDATED, principal, company.slug)
        return saved.sortedWith(compareBy<CompanyBusinessHour> { it.dayOfWeek.ordinal }.thenBy { it.displayOrder }).map { it.toSummary() }
    }

    @Transactional
    fun updateClosureDates(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<ClosureDateDraft>
    ): List<CompanyBackofficeClosureDateSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map {
            val startsOn = LocalDate.parse(it.startsOn)
            val endsOn = LocalDate.parse(it.endsOn)
            if (endsOn.isBefore(startsOn)) {
                throw IllegalArgumentException("Closure end date must be on or after the start date.")
            }
            NormalizedClosureDateDraft(
                label = it.label?.trim()?.ifBlank { null },
                startsOn = startsOn,
                endsOn = endsOn
            )
        }.sortedBy { it.startsOn }

        normalizedEntries.zipWithNext().forEach { (current, next) ->
            if (!current.endsOn.isBefore(next.startsOn)) {
                throw IllegalArgumentException("Closure periods cannot overlap.")
            }
        }

        companyClosureDateRepository.deleteAllByCompanyId(companyId)
        val saved = companyClosureDateRepository.saveAll(
            normalizedEntries.map {
                CompanyClosureDate(
                    company = company,
                    label = it.label,
                    startsOn = it.startsOn,
                    endsOn = it.endsOn
                )
            }
        )

        audit(SecurityAuditEventType.COMPANY_CLOSURE_DATES_UPDATED, principal, company.slug)
        return saved.map { it.toSummary() }
    }

    @Transactional
    fun updateNotificationPreferences(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        destinationEmail: String,
        notifyOnNewBooking: Boolean,
        notifyOnCancellation: Boolean,
        notifyDailySummary: Boolean
    ): CompanyBackofficeNotificationPreferencesSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        validateEmail(destinationEmail, "Notification destination must be a valid email address.")

        company.notificationDestinationEmail = destinationEmail.trim().lowercase()
        company.notifyOnNewBooking = notifyOnNewBooking
        company.notifyOnCancellation = notifyOnCancellation
        company.notifyDailySummary = notifyDailySummary

        audit(SecurityAuditEventType.COMPANY_NOTIFICATION_PREFERENCES_UPDATED, principal, company.slug)
        return company.toNotificationPreferencesSummary()
    }

    @Transactional(readOnly = true)
    fun listStaffUsers(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeStaffUserSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return companyMembershipRepository.findAllByCompanyId(requireNotNull(membership.company.id))
            .sortedBy { it.createdAt }
            .map { it.toStaffSummary() }
    }

    @Transactional
    fun createStaffUser(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        role: String
    ): CompanyBackofficeStaffUserSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val normalizedEmail = email.trim().lowercase()
        val normalizedRole = parseRole(role)

        if (fullName.trim().isBlank()) {
            throw IllegalArgumentException("Staff full name is required.")
        }
        validateEmail(normalizedEmail, "Staff email must be a valid email address.")
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw IllegalArgumentException("Email address is already in use.")
        }

        val user = userAccountRepository.save(
            UserAccount(
                email = normalizedEmail,
                fullName = fullName.trim(),
                passwordHash = passwordEncoder.encode(UUID.randomUUID().toString()),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )

        val savedMembership = companyMembershipRepository.save(
            CompanyMembership(
                company = company,
                user = user,
                role = normalizedRole
            )
        )

        val resetToken = passwordResetTokenRepository.save(
            PasswordResetToken(
                token = UUID.randomUUID().toString(),
                user = user,
                expiresAt = Instant.now().plus(registrationProperties.passwordResetTokenHours, ChronoUnit.HOURS)
            )
        )

        passwordResetMailSender.sendPasswordResetEmail(
            normalizedEmail,
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/${company.defaultLanguage}/reset-password?token=${resetToken.token}",
            company.defaultLanguage
        )

        audit(
            SecurityAuditEventType.COMPANY_STAFF_USER_CREATED,
            principal,
            company.slug,
            targetEmail = normalizedEmail
        )

        return savedMembership.toStaffSummary()
    }

    @Transactional
    fun updateStaffUser(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        membershipId: Long,
        role: String,
        status: String
    ): CompanyBackofficeStaffUserSummary {
        val actingMembership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = actingMembership.company
        val companyId = requireNotNull(company.id)
        val targetMembership = companyMembershipRepository.findByIdAndCompanyId(membershipId, companyId)
            ?: throw IllegalArgumentException("Staff user could not be found.")

        val nextRole = parseRole(role)
        val nextStatus = parseUserStatus(status)

        if (targetMembership.user.id == principal.userId && (nextRole != CompanyRole.COMPANY_ADMIN || nextStatus != UserStatus.ACTIVE)) {
            throw IllegalArgumentException("You cannot remove your own active company-admin access.")
        }

        val isCurrentlyAdmin = targetMembership.role == CompanyRole.COMPANY_ADMIN
        val isLosingAdminCoverage = isCurrentlyAdmin && nextRole != CompanyRole.COMPANY_ADMIN
        val isDisablingAdmin = isCurrentlyAdmin && nextStatus != UserStatus.ACTIVE
        val activeAdminCount = companyMembershipRepository.countByCompanyIdAndRoleAndUserStatus(
            companyId,
            CompanyRole.COMPANY_ADMIN,
            UserStatus.ACTIVE
        )
        if ((isLosingAdminCoverage || isDisablingAdmin) && activeAdminCount <= 1) {
            throw IllegalArgumentException("At least one company admin must remain active.")
        }

        targetMembership.role = nextRole
        targetMembership.user.status = nextStatus

        audit(
            SecurityAuditEventType.COMPANY_STAFF_USER_UPDATED,
            principal,
            company.slug,
            targetEmail = targetMembership.user.email
        )

        return targetMembership.toStaffSummary()
    }

    @Transactional
    fun updateCustomerQuestions(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<CustomerQuestionDraft>
    ): List<CompanyBackofficeCustomerQuestionSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map { entry ->
            val questionType = parseQuestionType(entry.questionType)
            val label = entry.label.trim()
            if (label.isBlank()) {
                throw IllegalArgumentException("Question label is required.")
            }
            val options = entry.options.map { it.trim() }.filter { it.isNotBlank() }
            if (questionType == CustomerQuestionType.SINGLE_SELECT && options.size < 2) {
                throw IllegalArgumentException("Selectable questions require at least two options.")
            }
            if (questionType != CustomerQuestionType.SINGLE_SELECT && options.isNotEmpty()) {
                throw IllegalArgumentException("Only selectable questions can define options.")
            }
            NormalizedCustomerQuestionDraft(
                label = label,
                questionType = questionType,
                required = entry.required,
                enabled = entry.enabled,
                displayOrder = entry.displayOrder,
                options = options
            )
        }.sortedBy { it.displayOrder }

        companyCustomerQuestionRepository.deleteAllByCompanyId(companyId)
        val saved = companyCustomerQuestionRepository.saveAll(
            normalizedEntries.map {
                CompanyCustomerQuestion(
                    company = company,
                    label = it.label,
                    questionType = it.questionType,
                    required = it.required,
                    enabled = it.enabled,
                    displayOrder = it.displayOrder,
                    optionsText = it.options.takeIf { options -> options.isNotEmpty() }?.joinToString("\n")
                )
            }
        )

        audit(SecurityAuditEventType.COMPANY_CUSTOMER_QUESTIONS_UPDATED, principal, company.slug)
        return saved.map { it.toSummary() }
    }

    @Transactional
    fun updateWidgetSettings(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        ctaLabel: String?,
        widgetEnabled: Boolean,
        allowedDomains: List<String>,
        themeVariant: String
    ): CompanyBackofficeWidgetSettingsSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val normalizedThemeVariant = themeVariant.trim().lowercase()
        if (normalizedThemeVariant !in setOf("minimal", "soft", "contrast")) {
            throw IllegalArgumentException("Widget theme variant is not supported.")
        }
        val normalizedDomains = allowedDomains.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        normalizedDomains.forEach { domain ->
            if (!Regex("^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}$").matches(domain)) {
                throw IllegalArgumentException("Allowed widget domains must be valid host names.")
            }
        }
        if (ctaLabel != null && ctaLabel.trim().length > 80) {
            throw IllegalArgumentException("Widget CTA label must be 80 characters or fewer.")
        }

        company.widgetCtaLabel = ctaLabel?.trim()?.ifBlank { null }
        company.widgetEnabled = widgetEnabled
        company.widgetAllowedDomains = normalizedDomains.joinToString("\n")
        company.widgetThemeVariant = normalizedThemeVariant

        audit(SecurityAuditEventType.COMPANY_WIDGET_SETTINGS_UPDATED, principal, company.slug)
        return company.toWidgetSettingsSummary()
    }

    private fun audit(
        eventType: SecurityAuditEventType,
        principal: AppAuthenticatedUser,
        companySlug: String,
        targetEmail: String? = null
    ) {
        securityAuditService.record(
            eventType = eventType,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = companySlug,
            targetEmail = targetEmail
        )
    }

    private fun parseDay(value: String): BusinessDay =
        runCatching { BusinessDay.valueOf(value.trim().uppercase()) }
            .getOrElse { throw IllegalArgumentException("Business day is not supported.") }

    private fun parseTime(value: String, message: String): LocalTime =
        runCatching { LocalTime.parse(value.trim()) }
            .getOrElse { throw IllegalArgumentException(message) }

    private fun parseRole(value: String): CompanyRole {
        val normalized = value.trim().uppercase()
        return when (normalized) {
            CompanyRole.COMPANY_ADMIN.name -> CompanyRole.COMPANY_ADMIN
            CompanyRole.STAFF.name -> CompanyRole.STAFF
            else -> throw IllegalArgumentException("Staff role is not supported.")
        }
    }

    private fun parseUserStatus(value: String): UserStatus {
        val normalized = value.trim().uppercase()
        return when (normalized) {
            UserStatus.ACTIVE.name -> UserStatus.ACTIVE
            UserStatus.INACTIVE.name -> UserStatus.INACTIVE
            else -> throw IllegalArgumentException("Staff status is not supported.")
        }
    }

    private fun parseQuestionType(value: String): CustomerQuestionType =
        runCatching { CustomerQuestionType.valueOf(value.trim().uppercase()) }
            .getOrElse { throw IllegalArgumentException("Question type is not supported.") }

    private fun validateEmail(value: String, message: String) {
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())) {
            throw IllegalArgumentException(message)
        }
    }

    private fun validatePhone(value: String, message: String) {
        if (!Regex("^[0-9+()\\-\\s]{7,}$").matches(value.trim())) {
            throw IllegalArgumentException(message)
        }
    }
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

private data class NormalizedBusinessHourDraft(
    val dayOfWeek: BusinessDay,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val displayOrder: Int
)

private data class NormalizedClosureDateDraft(
    val label: String?,
    val startsOn: LocalDate,
    val endsOn: LocalDate
)

private data class NormalizedCustomerQuestionDraft(
    val label: String,
    val questionType: CustomerQuestionType,
    val required: Boolean,
    val enabled: Boolean,
    val displayOrder: Int,
    val options: List<String>
)
