package com.reservenook.widget.application

import com.reservenook.booking.application.PublicBookingIntakeService
import com.reservenook.registration.infrastructure.CompanyRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

data class WidgetBootstrapPayload(
    val companyName: String,
    val companySlug: String,
    val locale: String,
    val themeVariant: String,
    val widgetToken: String,
    val iframeUrl: String
)

@Service
class WidgetBootstrapService(
    private val companyRepository: CompanyRepository,
    private val publicBookingIntakeService: PublicBookingIntakeService,
    private val widgetTokenService: WidgetTokenService,
    private val widgetUsageService: WidgetUsageService,
    @Value("\${NEXT_PUBLIC_APP_URL:http://localhost:3000}")
    private val appUrl: String
) {
    fun bootstrap(slug: String, locale: String?, origin: String?): WidgetBootstrapPayload {
        val company = companyRepository.findBySlug(slug)
            ?.takeIf { it.status == com.reservenook.registration.domain.CompanyStatus.ACTIVE }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Widget is unavailable.")
        val originHost = WidgetSupport.requireAllowedOrigin(company, origin)
        val resolvedLocale = locale?.trim()?.ifBlank { null } ?: company.defaultLanguage
        val config = publicBookingIntakeService.getConfig(slug)
        val token = widgetTokenService.issue(slug, originHost)
        widgetUsageService.record(company, originHost, com.reservenook.widget.domain.WidgetUsageEventType.BOOTSTRAP_INITIALIZED)
        return WidgetBootstrapPayload(
            companyName = config.displayName,
            companySlug = company.slug,
            locale = resolvedLocale,
            themeVariant = company.widgetThemeVariant,
            widgetToken = token,
            iframeUrl = "${appUrl.trimEnd('/')}/widget/${company.slug}?locale=$resolvedLocale&token=$token&theme=${company.widgetThemeVariant}"
        )
    }
}
