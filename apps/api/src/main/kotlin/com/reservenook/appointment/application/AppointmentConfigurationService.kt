package com.reservenook.appointment.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentProviderSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentServiceSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderScheduleSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ProviderAvailabilityDraft(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class PublicAppointmentSlotSummary(
    val serviceId: Long,
    val providerId: Long,
    val providerName: String,
    val startsAt: String,
    val endsAt: String
)

data class ProviderScheduleEntrySummary(
    val bookingId: Long,
    val customerName: String,
    val customerEmail: String,
    val status: String,
    val serviceName: String,
    val startsAt: String,
    val endsAt: String
)

@Service
class AppointmentConfigurationService(
    private val appointmentServiceManagementService: AppointmentServiceManagementService,
    private val appointmentProviderManagementService: AppointmentProviderManagementService,
    private val providerAvailabilityService: ProviderAvailabilityService,
    private val appointmentSlotGenerationService: AppointmentSlotGenerationService,
    private val publicAppointmentBookingService: PublicAppointmentBookingService,
    private val appointmentConfirmationService: AppointmentConfirmationService,
    private val providerScheduleQueryService: ProviderScheduleQueryService
) {

    @Transactional(readOnly = true)
    fun listServices(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeAppointmentServiceSummary> =
        appointmentServiceManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertService(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        serviceId: Long?,
        name: String,
        description: String?,
        durationMinutes: Int,
        bufferMinutes: Int,
        priceLabel: String?,
        enabled: Boolean,
        autoConfirm: Boolean
    ): CompanyBackofficeAppointmentServiceSummary =
        appointmentServiceManagementService.upsert(
            principal,
            requestedSlug,
            serviceId,
            name,
            description,
            durationMinutes,
            bufferMinutes,
            priceLabel,
            enabled,
            autoConfirm
        )

    @Transactional(readOnly = true)
    fun listProviders(principal: AppAuthenticatedUser, requestedSlug: String): Pair<List<CompanyBackofficeAppointmentProviderSummary>, List<CompanyBackofficeProviderScheduleSummary>> =
        appointmentProviderManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertProvider(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long?,
        linkedUserId: Long?,
        displayName: String,
        email: String?,
        active: Boolean
    ): CompanyBackofficeAppointmentProviderSummary =
        appointmentProviderManagementService.upsert(principal, requestedSlug, providerId, linkedUserId, displayName, email, active)

    @Transactional
    fun updateProviderAvailability(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long,
        entries: List<ProviderAvailabilityDraft>
    ): CompanyBackofficeProviderScheduleSummary =
        providerAvailabilityService.update(principal, requestedSlug, providerId, entries)

    @Transactional(readOnly = true)
    fun getPublicAvailability(slug: String, serviceId: Long, date: String, clientAddress: String): List<PublicAppointmentSlotSummary> =
        appointmentSlotGenerationService.getPublicAvailability(slug, serviceId, date, clientAddress)

    @Transactional
    fun bookPublicAppointment(
        slug: String,
        clientAddress: String,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        serviceId: Long,
        providerId: Long,
        startsAtIso: String
    ): CompanyBackofficeBookingSummary =
        publicAppointmentBookingService.book(
            slug,
            clientAddress,
            fullName,
            email,
            phone,
            preferredLanguage,
            serviceId,
            providerId,
            startsAtIso
        )

    @Transactional
    fun updateAppointmentConfirmation(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        bookingId: Long,
        status: String,
        internalNote: String?
    ): CompanyBackofficeBookingSummary =
        appointmentConfirmationService.update(principal, requestedSlug, bookingId, status, internalNote)

    @Transactional(readOnly = true)
    fun listProviderSchedule(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<ProviderScheduleEntrySummary> =
        providerScheduleQueryService.list(principal, requestedSlug, date)
}
