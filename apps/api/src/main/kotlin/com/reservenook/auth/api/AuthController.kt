package com.reservenook.auth.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.LoginService
import com.reservenook.auth.application.RequestPasswordResetService
import com.reservenook.auth.application.ResetPasswordService
import com.reservenook.security.application.RequestFingerprintResolver
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val loginService: LoginService,
    private val requestPasswordResetService: RequestPasswordResetService,
    private val resetPasswordService: ResetPasswordService,
    private val securityAuditService: SecurityAuditService
) {

    @PostMapping("/api/public/auth/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): LoginResponse {
        val result = loginService.login(
            email = request.email,
            password = request.password,
            request = httpServletRequest,
            response = httpServletResponse
        )

        return LoginResponse(redirectTo = result.redirectTo)
    }

    @PostMapping("/api/public/auth/forgot-password")
    fun requestPasswordReset(
        @Valid @RequestBody request: RequestPasswordResetRequest,
        httpServletRequest: HttpServletRequest
    ): RequestPasswordResetResponse {
        val result = requestPasswordResetService.request(
            request.email,
            RequestFingerprintResolver.resolve(httpServletRequest, request.email)
        )
        return RequestPasswordResetResponse(result.message)
    }

    @PostMapping("/api/public/auth/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResetPasswordResponse {
        val result = resetPasswordService.reset(
            token = request.token,
            password = request.password
        )

        return ResetPasswordResponse(
            message = result.message,
            redirectTo = result.redirectTo
        )
    }

    @GetMapping("/api/auth/session")
    fun currentSession(@AuthenticationPrincipal principal: AppAuthenticatedUser): SessionResponse {
        return SessionResponse(
            userId = principal.userId,
            email = principal.email,
            isPlatformAdmin = principal.isPlatformAdmin,
            companySlug = principal.companySlug,
            companyRole = principal.companyRole,
            redirectTo = if (principal.isPlatformAdmin) "/platform-admin" else "/app/company/${principal.companySlug}"
        )
    }

    @GetMapping("/api/auth/csrf-token")
    fun csrfToken(csrfToken: CsrfToken): CsrfTokenResponse = CsrfTokenResponse(token = csrfToken.token)

    @PostMapping("/api/auth/logout")
    fun logout(@AuthenticationPrincipal principal: AppAuthenticatedUser?, session: HttpSession?): LoginResponse {
        if (principal != null) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.LOGOUT_SUCCESS,
                outcome = SecurityAuditOutcome.SUCCESS,
                actorUserId = principal.userId,
                actorEmail = principal.email,
                companySlug = principal.companySlug
            )
        }
        session?.invalidate()
        SecurityContextHolder.clearContext()

        return LoginResponse(redirectTo = "/en/login")
    }
}
