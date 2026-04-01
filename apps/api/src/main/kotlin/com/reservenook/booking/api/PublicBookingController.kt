package com.reservenook.booking.api

import com.reservenook.booking.application.PublicBookingIntakeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicBookingController(
    private val publicBookingIntakeService: PublicBookingIntakeService
) {

    @GetMapping("/api/public/companies/{slug}/booking-intake-config")
    fun bookingIntakeConfig(@PathVariable slug: String): PublicBookingIntakeConfigResponse =
        publicBookingIntakeService.getConfig(slug)

    @PostMapping("/api/public/companies/{slug}/booking-intake")
    fun submitBookingIntake(
        @PathVariable slug: String,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestBody request: SubmitPublicBookingIntakeRequest,
        servletRequest: jakarta.servlet.http.HttpServletRequest
    ): SubmitPublicBookingIntakeResponse {
        val clientAddress = forwardedFor?.substringBefore(",")?.trim()
            ?: realIp?.trim()
            ?: servletRequest.remoteAddr
        return publicBookingIntakeService.submit(slug, clientAddress, request)
    }
}

data class PublicBookingIntakeConfigResponse(
    val companyName: String,
    val companySlug: String,
    val displayName: String,
    val defaultLanguage: String,
    val defaultLocale: String,
    val ctaLabel: String,
    val bookingEnabled: Boolean,
    val customerQuestions: List<PublicBookingQuestionSummary>
)

data class PublicBookingQuestionSummary(
    val label: String,
    val questionType: String,
    val required: Boolean,
    val options: List<String>
)

data class SubmitPublicBookingIntakeRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val requestSummary: String?,
    val preferredDate: String?,
    val notes: String?
)

data class SubmitPublicBookingIntakeResponse(
    val message: String
)
