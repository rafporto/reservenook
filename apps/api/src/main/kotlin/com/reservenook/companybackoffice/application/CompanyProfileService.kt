package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeCompanySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProfileSummary
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CompanyProfileService(
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun updateProfile(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        companyName: String,
        businessDescription: String?,
        contactEmail: String,
        contactPhone: String,
        addressLine1: String,
        addressLine2: String?,
        city: String,
        postalCode: String,
        countryCode: String
    ): UpdatedCompanyProfile {
        if (principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        val membership = companyMembershipRepository.findFirstByUserEmailAndCompanySlug(principal.email, requestedSlug)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")

        if (membership.role != CompanyRole.COMPANY_ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }

        validateProfile(
            companyName = companyName,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            addressLine1 = addressLine1,
            city = city,
            postalCode = postalCode,
            countryCode = countryCode
        )

        val company = membership.company
        company.name = companyName.trim()
        company.businessDescription = businessDescription?.trim()?.ifBlank { null }
        company.contactEmail = contactEmail.trim()
        company.contactPhone = contactPhone.trim()
        company.addressLine1 = addressLine1.trim()
        company.addressLine2 = addressLine2?.trim()?.ifBlank { null }
        company.city = city.trim()
        company.postalCode = postalCode.trim()
        company.countryCode = countryCode.trim().uppercase()

        securityAuditService.record(
            eventType = SecurityAuditEventType.COMPANY_PROFILE_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug
        )

        return UpdatedCompanyProfile(
            company = company.toCompanySummary(),
            profile = company.toProfileSummary()
        )
    }

    private fun validateProfile(
        companyName: String,
        contactEmail: String,
        contactPhone: String,
        addressLine1: String,
        city: String,
        postalCode: String,
        countryCode: String
    ) {
        if (companyName.isBlank()) {
            throw IllegalArgumentException("Company name is required.")
        }

        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(contactEmail.trim())) {
            throw IllegalArgumentException("Contact email must be a valid email address.")
        }

        if (!Regex("^[0-9+()\\-\\s]{7,}$").matches(contactPhone.trim())) {
            throw IllegalArgumentException("Contact phone must be a valid phone number.")
        }

        if (addressLine1.isBlank()) {
            throw IllegalArgumentException("Address line 1 is required.")
        }

        if (city.isBlank()) {
            throw IllegalArgumentException("City is required.")
        }

        if (postalCode.isBlank()) {
            throw IllegalArgumentException("Postal code is required.")
        }

        if (!Regex("^[A-Za-z]{2}$").matches(countryCode.trim())) {
            throw IllegalArgumentException("Country code must use the ISO 2-letter format.")
        }
    }
}

data class UpdatedCompanyProfile(
    val company: CompanyBackofficeCompanySummary,
    val profile: CompanyBackofficeProfileSummary
)

private fun Company.toCompanySummary() = CompanyBackofficeCompanySummary(
    companyName = name,
    companySlug = slug,
    businessType = businessType.name,
    companyStatus = status.name,
    defaultLanguage = defaultLanguage,
    defaultLocale = defaultLocale,
    createdAt = createdAt.toString()
)

private fun Company.toProfileSummary() = CompanyBackofficeProfileSummary(
    businessDescription = businessDescription,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    postalCode = postalCode,
    countryCode = countryCode
)
