package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeResponse
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CompanyBackofficeAccessService(
    private val companyMembershipRepository: CompanyMembershipRepository
) {

    fun getBackoffice(principal: AppAuthenticatedUser, requestedSlug: String): CompanyBackofficeResponse {
        if (principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        val membership = companyMembershipRepository.findFirstByUserEmailAndCompanySlug(principal.email, requestedSlug)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")

        if (membership.role != CompanyRole.COMPANY_ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        return CompanyBackofficeResponse(
            companyName = membership.company.name,
            companySlug = membership.company.slug,
            businessType = membership.company.businessType.name,
            role = membership.role.name,
            currentUserEmail = principal.email
        )
    }
}
