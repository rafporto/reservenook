package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentProviderAvailability
import com.reservenook.appointment.infrastructure.AppointmentProviderAvailabilityRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderScheduleSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toScheduleSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProviderAvailabilityService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val appointmentProviderAvailabilityRepository: AppointmentProviderAvailabilityRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun listForProvider(providerId: Long): List<AppointmentProviderAvailability> =
        appointmentProviderAvailabilityRepository.findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(providerId)

    @Transactional(readOnly = true)
    fun listForProviders(providerIds: Collection<Long>): Map<Long, List<AppointmentProviderAvailability>> {
        if (providerIds.isEmpty()) {
            return emptyMap()
        }
        return appointmentProviderAvailabilityRepository
            .findAllByProviderIdInOrderByProviderIdAscDayOfWeekAscDisplayOrderAsc(providerIds)
            .groupBy { requireNotNull(it.provider.id) }
    }

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long,
        entries: List<ProviderAvailabilityDraft>
    ): CompanyBackofficeProviderScheduleSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val provider = appointmentProviderRepository.findByIdAndCompanyId(providerId, requireNotNull(membership.company.id))
            ?: throw IllegalArgumentException("Provider could not be found.")

        val normalized = entries.map {
            val day = AppointmentSupport.parseDay(it.dayOfWeek)
            val opens = AppointmentSupport.parseTime(it.opensAt, "Opening time must use HH:mm format.")
            val closes = AppointmentSupport.parseTime(it.closesAt, "Closing time must use HH:mm format.")
            if (!opens.isBefore(closes)) throw IllegalArgumentException("Closing time must be after opening time.")
            AppointmentProviderAvailability(provider = provider, dayOfWeek = day, opensAt = opens, closesAt = closes, displayOrder = it.displayOrder)
        }
        normalized.groupBy { it.dayOfWeek }.forEach { (_, perDay) ->
            val sorted = perDay.sortedBy { it.opensAt }
            sorted.zipWithNext().forEach { (current, next) ->
                if (!current.closesAt.isBefore(next.opensAt)) {
                    throw IllegalArgumentException("Provider availability windows cannot overlap on the same day.")
                }
            }
        }

        appointmentProviderAvailabilityRepository.deleteAllByProviderId(providerId)
        val saved = appointmentProviderAvailabilityRepository.saveAll(normalized)
        securityAuditService.record(
            eventType = SecurityAuditEventType.APPOINTMENT_PROVIDER_AVAILABILITY_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = membership.company.slug,
            details = provider.displayName
        )
        return provider.toScheduleSummary(saved)
    }
}
