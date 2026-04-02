package com.reservenook.widget.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.widget.application.WidgetBootstrapService
import com.reservenook.widget.application.WidgetUsageService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class WidgetController(
    private val widgetBootstrapService: WidgetBootstrapService,
    private val widgetUsageService: WidgetUsageService
) {
    @GetMapping("/api/public/widget/{slug}/bootstrap")
    fun bootstrap(
        @PathVariable slug: String,
        @RequestParam(required = false) locale: String?,
        @RequestHeader(value = "Origin", required = false) origin: String?,
        response: HttpServletResponse
    ): WidgetBootstrapResponse {
        val payload = widgetBootstrapService.bootstrap(slug, locale, origin)
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin)
            response.setHeader("Vary", "Origin")
        }
        return WidgetBootstrapResponse(
            companyName = payload.companyName,
            companySlug = payload.companySlug,
            locale = payload.locale,
            themeVariant = payload.themeVariant,
            iframeUrl = payload.iframeUrl,
            widgetToken = payload.widgetToken
        )
    }

    @GetMapping("/api/app/company/{slug}/widget-usage")
    fun usage(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ): WidgetUsageResponse {
        val summary = widgetUsageService.getSummary(principal, slug)
        return WidgetUsageResponse(
            bootstrapsLast7Days = summary.bootstrapsLast7Days,
            bookingsLast7Days = summary.bookingsLast7Days,
            recentOrigins = summary.recentOrigins.map {
                WidgetUsageOriginResponse(
                    originHost = it.originHost,
                    bootstrapCount = it.bootstrapCount,
                    bookingCount = it.bookingCount
                )
            }
        )
    }
}

data class WidgetBootstrapResponse(
    val companyName: String,
    val companySlug: String,
    val locale: String,
    val themeVariant: String,
    val iframeUrl: String,
    val widgetToken: String
)

data class WidgetUsageResponse(
    val bootstrapsLast7Days: Int,
    val bookingsLast7Days: Int,
    val recentOrigins: List<WidgetUsageOriginResponse>
)

data class WidgetUsageOriginResponse(
    val originHost: String,
    val bootstrapCount: Int,
    val bookingCount: Int
)
