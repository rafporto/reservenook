package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class CompanyAdminAccessService(
    private val companyMembershipRepository: CompanyMembershipRepository
) {

    fun requireCompanyMember(principal: AppAuthenticatedUser, requestedSlug: String): CompanyMembership {
        if (principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        return companyMembershipRepository.findFirstByUserEmailAndCompanySlug(principal.email, requestedSlug)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
    }

    fun requireCompanyAdmin(principal: AppAuthenticatedUser, requestedSlug: String): CompanyMembership {
        val membership = requireCompanyMember(principal, requestedSlug)

        if (membership.role != CompanyRole.COMPANY_ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        return membership
    }
}
