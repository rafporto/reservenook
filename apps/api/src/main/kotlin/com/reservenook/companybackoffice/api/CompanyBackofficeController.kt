package com.reservenook.companybackoffice.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyBackofficeAccessService
import com.reservenook.companybackoffice.application.CompanyProfileService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CompanyBackofficeController(
    private val companyBackofficeAccessService: CompanyBackofficeAccessService,
    private val companyProfileService: CompanyProfileService
) {

    @GetMapping("/api/app/company/{slug}/backoffice")
    fun backoffice(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ): CompanyBackofficeResponse = companyBackofficeAccessService.getBackoffice(principal, slug)

    @PutMapping("/api/app/company/{slug}/profile")
    fun updateProfile(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @Valid @RequestBody request: UpdateCompanyProfileRequest
    ): UpdateCompanyProfileResponse {
        val updatedProfile = companyProfileService.updateProfile(
            principal = principal,
            requestedSlug = slug,
            companyName = request.companyName,
            businessDescription = request.businessDescription,
            contactEmail = request.contactEmail,
            contactPhone = request.contactPhone,
            addressLine1 = request.addressLine1,
            addressLine2 = request.addressLine2,
            city = request.city,
            postalCode = request.postalCode,
            countryCode = request.countryCode
        )

        return UpdateCompanyProfileResponse(
            message = "Company profile updated.",
            company = updatedProfile.company,
            profile = updatedProfile.profile
        )
    }
}
