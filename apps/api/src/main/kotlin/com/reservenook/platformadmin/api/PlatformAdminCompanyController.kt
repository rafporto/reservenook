package com.reservenook.platformadmin.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.application.PlatformInactivityPolicyService
import com.reservenook.platformadmin.application.PlatformAdminCompanyListService
import com.reservenook.security.application.RecentAuthenticationGuard
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PlatformAdminCompanyController(
    private val platformAdminCompanyListService: PlatformAdminCompanyListService,
    private val platformInactivityPolicyService: PlatformInactivityPolicyService,
    private val recentAuthenticationGuard: RecentAuthenticationGuard
) {

    @GetMapping("/api/platform-admin/companies")
    fun listCompanies(@AuthenticationPrincipal principal: AppAuthenticatedUser): PlatformAdminCompanyListResponse {
        return PlatformAdminCompanyListResponse(
            companies = platformAdminCompanyListService.listCompanies(principal)
        )
    }

    @GetMapping("/api/platform-admin/inactivity-policy")
    fun getInactivityPolicy(@AuthenticationPrincipal principal: AppAuthenticatedUser): InactivityPolicyResponse {
        return platformInactivityPolicyService.getPolicy(principal)
    }

    @PutMapping("/api/platform-admin/inactivity-policy")
    fun updateInactivityPolicy(
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateInactivityPolicyRequest
    ): UpdateInactivityPolicyResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        val policy = platformInactivityPolicyService.updatePolicy(
            principal = principal,
            inactivityThresholdDays = request.inactivityThresholdDays,
            deletionWarningLeadDays = request.deletionWarningLeadDays
        )

        return UpdateInactivityPolicyResponse(
            message = "Inactivity policy updated.",
            policy = policy
        )
    }
}
