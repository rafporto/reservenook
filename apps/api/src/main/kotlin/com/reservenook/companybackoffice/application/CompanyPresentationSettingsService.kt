package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeBrandingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeLocalizationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeWidgetSettingsSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class CompanyPresentationSettingsService(
    private val companyAdminAccessService: CompanyAdminAccessService,
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

        company.brandDisplayName = displayName?.trim()?.ifBlank { null }
        company.brandLogoUrl = logoUrl?.trim()?.ifBlank { null }
        company.brandAccentColor = accentColor.trim().uppercase()
        company.supportEmail = CommonInputValidation.requireEmail(supportEmail, "Support email must be a valid email address.")
        company.supportPhone = CommonInputValidation.requireOptionalPhone(supportPhone, "Support phone must be a valid phone number.")
            ?: throw IllegalArgumentException("Support phone must be a valid phone number.")

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
        val normalizedDomains = CommonInputValidation.requireDomains(
            allowedDomains,
            "Allowed widget domains must be valid host names."
        )
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

    private fun audit(eventType: SecurityAuditEventType, principal: AppAuthenticatedUser, companySlug: String) {
        securityAuditService.record(
            eventType = eventType,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = companySlug
        )
    }
}
