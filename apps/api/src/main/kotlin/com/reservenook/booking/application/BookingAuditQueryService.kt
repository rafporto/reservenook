package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingAuditSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingAuditQueryService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val bookingAuditEventRepository: BookingAuditEventRepository
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeBookingAuditSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return bookingAuditEventRepository.findAllByCompanyIdOrderByCreatedAtDesc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }
}
