package com.reservenook.platformadmin.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.application.PlatformAdminCompanyListService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PlatformAdminCompanyController(
    private val platformAdminCompanyListService: PlatformAdminCompanyListService
) {

    @GetMapping("/api/platform-admin/companies")
    fun listCompanies(@AuthenticationPrincipal principal: AppAuthenticatedUser): PlatformAdminCompanyListResponse {
        return PlatformAdminCompanyListResponse(
            companies = platformAdminCompanyListService.listCompanies(principal)
        )
    }
}
