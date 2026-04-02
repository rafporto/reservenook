package com.reservenook.platformadmin.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.application.AbusePreventionPolicyService
import com.reservenook.platformadmin.application.PlatformInactivityPolicyService
import com.reservenook.platformadmin.application.PlatformAdminCompanyListService
import com.reservenook.platformadmin.application.PlatformRetentionService
import com.reservenook.security.application.RecentAuthenticationGuard
import com.reservenook.security.application.SecurityAuditQueryService
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class PlatformAdminCompanyController(
    private val platformAdminCompanyListService: PlatformAdminCompanyListService,
    private val platformInactivityPolicyService: PlatformInactivityPolicyService,
    private val abusePreventionPolicyService: AbusePreventionPolicyService,
    private val platformRetentionService: PlatformRetentionService,
    private val securityAuditQueryService: SecurityAuditQueryService,
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

    @GetMapping("/api/platform-admin/abuse-policy")
    fun getAbusePolicy(@AuthenticationPrincipal principal: AppAuthenticatedUser): AbusePreventionPolicyResponse {
        return abusePreventionPolicyService.getPolicy(principal)
    }

    @GetMapping("/api/platform-admin/operations-summary")
    fun getOperationsSummary(@AuthenticationPrincipal principal: AppAuthenticatedUser): PlatformOperationsResponse {
        return PlatformOperationsResponse(
            summary = securityAuditQueryService.getPlatformSummary(principal),
            securityAudit = securityAuditQueryService.listPlatformAudit(principal)
        )
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

    @PutMapping("/api/platform-admin/abuse-policy")
    fun updateAbusePolicy(
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateAbusePreventionPolicyRequest
    ): UpdateAbusePreventionPolicyResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        return UpdateAbusePreventionPolicyResponse(
            message = "Abuse prevention policy updated.",
            policy = abusePreventionPolicyService.updatePolicy(
                principal = principal,
                loginPairLimit = request.loginPairLimit,
                loginClientLimit = request.loginClientLimit,
                loginEmailLimit = request.loginEmailLimit,
                publicWritePairLimit = request.publicWritePairLimit,
                publicWriteClientLimit = request.publicWriteClientLimit,
                publicWriteEmailLimit = request.publicWriteEmailLimit,
                publicReadClientLimit = request.publicReadClientLimit
            )
        )
    }

    @PutMapping("/api/platform-admin/companies/{slug}/retention")
    fun updateCompanyLegalHold(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @Valid @RequestBody request: UpdateCompanyLegalHoldRequest
    ): UpdateCompanyLegalHoldResponse {
        recentAuthenticationGuard.requireRecentAuthentication(session)
        val legalHoldUntil = request.legalHoldUntil?.takeIf { it.isNotBlank() }?.let(Instant::parse)
        val updated = platformRetentionService.updateLegalHold(principal, slug, legalHoldUntil)
        return UpdateCompanyLegalHoldResponse(
            message = if (updated == null) "Company legal hold cleared." else "Company legal hold updated.",
            companySlug = slug,
            legalHoldUntil = updated?.toString()
        )
    }
}
