package com.reservenook.appointment.api

import com.reservenook.appointment.application.AppointmentConfigurationService
import com.reservenook.appointment.application.ProviderAvailabilityDraft
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.AppointmentProvidersResponse
import com.reservenook.companybackoffice.api.AppointmentServicesResponse
import com.reservenook.companybackoffice.api.UpsertAppointmentProviderRequest
import com.reservenook.companybackoffice.api.UpsertAppointmentProviderResponse
import com.reservenook.companybackoffice.api.UpsertAppointmentServiceRequest
import com.reservenook.companybackoffice.api.UpsertAppointmentServiceResponse
import com.reservenook.companybackoffice.api.UpdateAppointmentProviderAvailabilityRequest
import com.reservenook.companybackoffice.api.UpdateAppointmentProviderAvailabilityResponse
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AppointmentController(
    private val appointmentConfigurationService: AppointmentConfigurationService,
    private val recentAuthenticationGuard: com.reservenook.security.application.RecentAuthenticationGuard
) {

    @GetMapping("/api/app/company/{slug}/appointment-services")
    fun listServices(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = AppointmentServicesResponse(
        appointmentServices = appointmentConfigurationService.listServices(principal, slug)
    )

    @PostMapping("/api/app/company/{slug}/appointment-services")
    fun createService(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertAppointmentServiceRequest
    ) = UpsertAppointmentServiceResponse(
        message = "Appointment service saved.",
        appointmentService = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            appointmentConfigurationService.upsertService(
                principal, slug, null, request.name, request.description, request.durationMinutes,
                request.bufferMinutes, request.priceLabel, request.enabled, request.autoConfirm
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/appointment-services/{serviceId}")
    fun updateService(
        @PathVariable slug: String,
        @PathVariable serviceId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertAppointmentServiceRequest
    ) = UpsertAppointmentServiceResponse(
        message = "Appointment service updated.",
        appointmentService = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            appointmentConfigurationService.upsertService(
                principal, slug, serviceId, request.name, request.description, request.durationMinutes,
                request.bufferMinutes, request.priceLabel, request.enabled, request.autoConfirm
            )
        }
    )

    @GetMapping("/api/app/company/{slug}/appointment-providers")
    fun listProviders(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ): AppointmentProvidersResponse {
        val (providers, schedules) = appointmentConfigurationService.listProviders(principal, slug)
        return AppointmentProvidersResponse(providers, schedules)
    }

    @PostMapping("/api/app/company/{slug}/appointment-providers")
    fun createProvider(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertAppointmentProviderRequest
    ) = UpsertAppointmentProviderResponse(
        message = "Appointment provider saved.",
        appointmentProvider = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            appointmentConfigurationService.upsertProvider(
                principal, slug, null, request.linkedUserId, request.displayName, request.email, request.active
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/appointment-providers/{providerId}")
    fun updateProvider(
        @PathVariable slug: String,
        @PathVariable providerId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertAppointmentProviderRequest
    ) = UpsertAppointmentProviderResponse(
        message = "Appointment provider updated.",
        appointmentProvider = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            appointmentConfigurationService.upsertProvider(
                principal, slug, providerId, request.linkedUserId, request.displayName, request.email, request.active
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/appointment-providers/{providerId}/availability")
    fun updateProviderAvailability(
        @PathVariable slug: String,
        @PathVariable providerId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpdateAppointmentProviderAvailabilityRequest
    ) = UpdateAppointmentProviderAvailabilityResponse(
        message = "Provider availability updated.",
        providerSchedule = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            appointmentConfigurationService.updateProviderAvailability(
                principal, slug, providerId,
                request.entries.map { ProviderAvailabilityDraft(it.dayOfWeek, it.opensAt, it.closesAt, it.displayOrder) }
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/appointments/{bookingId}/confirmation")
    fun updateAppointmentConfirmation(
        @PathVariable slug: String,
        @PathVariable bookingId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestBody request: com.reservenook.companybackoffice.api.UpdateBookingStatusRequest
    ) = com.reservenook.companybackoffice.api.UpdateBookingStatusResponse(
        message = "Appointment confirmation updated.",
        booking = appointmentConfigurationService.updateAppointmentConfirmation(
            principal, slug, bookingId, request.status, request.internalNote
        )
    )

    @GetMapping("/api/app/company/{slug}/providers/me/schedule")
    fun mySchedule(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestParam date: String
    ) = ProviderScheduleResponse(
        entries = appointmentConfigurationService.listProviderSchedule(principal, slug, date)
    )
}

data class ProviderScheduleResponse(
    val entries: List<com.reservenook.appointment.application.ProviderScheduleEntrySummary>
)
