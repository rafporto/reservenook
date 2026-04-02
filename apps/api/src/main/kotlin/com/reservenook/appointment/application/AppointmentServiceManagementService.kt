package com.reservenook.appointment.application

import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentServiceSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AppointmentServiceManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val appointmentServiceRepository: AppointmentServiceRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeAppointmentServiceSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return appointmentServiceRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun upsert(
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
    ): CompanyBackofficeAppointmentServiceSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        if (name.trim().isBlank()) throw IllegalArgumentException("Appointment service name is required.")
        if (durationMinutes !in 15..480) throw IllegalArgumentException("Service duration must be between 15 and 480 minutes.")
        if (bufferMinutes !in 0..180) throw IllegalArgumentException("Service buffer must be between 0 and 180 minutes.")

        val service = serviceId?.let { appointmentServiceRepository.findByIdAndCompanyId(it, requireNotNull(company.id)) }
            ?: AppointmentService(company = company, name = name.trim(), durationMinutes = durationMinutes)

        service.name = name.trim()
        service.description = description?.trim()?.ifBlank { null }
        service.durationMinutes = durationMinutes
        service.bufferMinutes = bufferMinutes
        service.priceLabel = priceLabel?.trim()?.ifBlank { null }
        service.enabled = enabled
        service.autoConfirm = autoConfirm
        service.updatedAt = Instant.now()

        val saved = appointmentServiceRepository.save(service)
        securityAuditService.record(
            eventType = SecurityAuditEventType.APPOINTMENT_SERVICE_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            details = saved.name
        )
        return saved.toSummary()
    }
}
