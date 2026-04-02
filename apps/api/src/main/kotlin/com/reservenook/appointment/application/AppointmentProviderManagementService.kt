package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentProviderSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderScheduleSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toScheduleSummary
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AppointmentProviderManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val providerAvailabilityService: ProviderAvailabilityService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): Pair<List<CompanyBackofficeAppointmentProviderSummary>, List<CompanyBackofficeProviderScheduleSummary>> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val providers = appointmentProviderRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id))
        return providers.map { it.toSummary() } to providers.map { provider ->
            provider.toScheduleSummary(providerAvailabilityService.listForProvider(requireNotNull(provider.id)))
        }
    }

    @Transactional
    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        providerId: Long?,
        linkedUserId: Long?,
        displayName: String,
        email: String?,
        active: Boolean
    ): CompanyBackofficeAppointmentProviderSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        if (displayName.trim().isBlank()) throw IllegalArgumentException("Provider display name is required.")
        val linkedMembership = linkedUserId?.let {
            val tenantMembership = companyMembershipRepository.findFirstByUserIdAndCompanySlug(it, requestedSlug)
                ?: throw IllegalArgumentException("Linked provider user must belong to the same company.")
            tenantMembership.user
        }
        val normalizedEmail = email?.trim()?.ifBlank { null }?.let {
            CommonInputValidation.requireEmail(it, "Provider email must be a valid email address.")
        }

        val provider = providerId?.let { appointmentProviderRepository.findByIdAndCompanyId(it, requireNotNull(company.id)) }
            ?: AppointmentProvider(company = company, displayName = displayName.trim())
        provider.linkedUser = linkedMembership
        provider.displayName = displayName.trim()
        provider.email = normalizedEmail
        provider.active = active
        provider.updatedAt = Instant.now()

        val saved = appointmentProviderRepository.save(provider)
        securityAuditService.record(
            eventType = SecurityAuditEventType.APPOINTMENT_PROVIDER_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            details = saved.displayName
        )
        return saved.toSummary()
    }
}
