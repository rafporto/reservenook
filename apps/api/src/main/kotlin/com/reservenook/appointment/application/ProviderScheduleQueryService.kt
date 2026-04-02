package com.reservenook.appointment.application

import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class ProviderScheduleQueryService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val appointmentProviderRepository: AppointmentProviderRepository,
    private val appointmentBookingRepository: AppointmentBookingRepository
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<ProviderScheduleEntrySummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val provider = appointmentProviderRepository.findFirstByCompanyIdAndLinkedUserId(requireNotNull(membership.company.id), principal.userId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        val localDate = LocalDate.parse(date)
        val start = localDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = localDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return appointmentBookingRepository.findAllByProviderIdAndStartsAtBetweenOrderByStartsAtAsc(requireNotNull(provider.id), start, end).map {
            ProviderScheduleEntrySummary(
                bookingId = requireNotNull(it.booking.id),
                customerName = it.booking.customerContact.fullName,
                customerEmail = it.booking.customerContact.email,
                status = it.booking.status.name,
                serviceName = it.appointmentService.name,
                startsAt = it.startsAt.toString(),
                endsAt = it.endsAt.toString()
            )
        }
    }
}
