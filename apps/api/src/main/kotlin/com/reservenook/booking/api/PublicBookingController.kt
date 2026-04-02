package com.reservenook.booking.api

import com.reservenook.appointment.application.AppointmentConfigurationService
import com.reservenook.booking.application.PublicBookingIntakeService
import com.reservenook.groupclass.application.GroupClassConfigurationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PublicBookingController(
    private val publicBookingIntakeService: PublicBookingIntakeService,
    private val appointmentConfigurationService: AppointmentConfigurationService,
    private val groupClassConfigurationService: GroupClassConfigurationService
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

    @GetMapping("/api/public/companies/{slug}/appointments/availability")
    fun appointmentAvailability(
        @PathVariable slug: String,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestParam serviceId: Long,
        @RequestParam date: String,
        servletRequest: jakarta.servlet.http.HttpServletRequest
    ) = PublicAppointmentAvailabilityResponse(
        slots = appointmentConfigurationService.getPublicAvailability(
            slug = slug,
            serviceId = serviceId,
            date = date,
            clientAddress = forwardedFor?.substringBefore(",")?.trim() ?: realIp?.trim() ?: servletRequest.remoteAddr
        )
    )

    @PostMapping("/api/public/companies/{slug}/appointments/book")
    fun bookAppointment(
        @PathVariable slug: String,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestBody request: BookPublicAppointmentRequest,
        servletRequest: jakarta.servlet.http.HttpServletRequest
    ) = BookPublicAppointmentResponse(
        message = "Your appointment request has been received.",
        booking = appointmentConfigurationService.bookPublicAppointment(
            slug = slug,
            clientAddress = forwardedFor?.substringBefore(",")?.trim() ?: realIp?.trim() ?: servletRequest.remoteAddr,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            preferredLanguage = request.preferredLanguage,
            serviceId = request.serviceId,
            providerId = request.providerId,
            startsAtIso = request.startsAt
        )
    )

    @GetMapping("/api/public/companies/{slug}/classes/availability")
    fun classAvailability(
        @PathVariable slug: String,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestParam classTypeId: Long,
        servletRequest: jakarta.servlet.http.HttpServletRequest
    ) = PublicClassAvailabilityResponse(
        sessions = groupClassConfigurationService.getPublicAvailability(
            slug = slug,
            classTypeId = classTypeId,
            clientAddress = forwardedFor?.substringBefore(",")?.trim() ?: realIp?.trim() ?: servletRequest.remoteAddr
        )
    )

    @PostMapping("/api/public/companies/{slug}/classes/book")
    fun bookClass(
        @PathVariable slug: String,
        @RequestHeader(value = "X-Forwarded-For", required = false) forwardedFor: String?,
        @RequestHeader(value = "X-Real-IP", required = false) realIp: String?,
        @RequestBody request: BookPublicClassRequest,
        servletRequest: jakarta.servlet.http.HttpServletRequest
    ) = BookPublicClassResponse(
        message = "Your class booking request has been received.",
        classBooking = groupClassConfigurationService.bookPublicClass(
            slug = slug,
            clientAddress = forwardedFor?.substringBefore(",")?.trim() ?: realIp?.trim() ?: servletRequest.remoteAddr,
            sessionId = request.sessionId,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            preferredLanguage = request.preferredLanguage
        )
    )
}

data class PublicBookingIntakeConfigResponse(
    val companyName: String,
    val companySlug: String,
    val businessType: String,
    val displayName: String,
    val defaultLanguage: String,
    val defaultLocale: String,
    val ctaLabel: String,
    val bookingEnabled: Boolean,
    val customerQuestions: List<PublicBookingQuestionSummary>,
    val appointmentServices: List<PublicAppointmentServiceSummary>,
    val classTypes: List<PublicClassTypeSummary>
)

data class PublicBookingQuestionSummary(
    val label: String,
    val questionType: String,
    val required: Boolean,
    val options: List<String>
)

data class PublicAppointmentServiceSummary(
    val id: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val priceLabel: String?
)

data class PublicClassTypeSummary(
    val id: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val defaultCapacity: Int
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

data class PublicAppointmentAvailabilityResponse(
    val slots: List<com.reservenook.appointment.application.PublicAppointmentSlotSummary>
)

data class PublicClassAvailabilityResponse(
    val sessions: List<com.reservenook.groupclass.application.PublicClassSessionSummary>
)

data class BookPublicAppointmentRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val serviceId: Long,
    val providerId: Long,
    val startsAt: String
)

data class BookPublicAppointmentResponse(
    val message: String,
    val booking: com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
)

data class BookPublicClassRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val sessionId: Long
)

data class BookPublicClassResponse(
    val message: String,
    val classBooking: com.reservenook.companybackoffice.api.CompanyBackofficeClassBookingSummary
)
