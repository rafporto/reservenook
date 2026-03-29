package com.reservenook.companybackoffice.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyBackofficeAccessService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class CompanyBackofficeController(
    private val companyBackofficeAccessService: CompanyBackofficeAccessService
) {

    @GetMapping("/api/app/company/{slug}/backoffice")
    fun backoffice(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ): CompanyBackofficeResponse = companyBackofficeAccessService.getBackoffice(principal, slug)
}
