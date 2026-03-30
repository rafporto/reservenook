package com.reservenook.auth.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.LoginService
import com.reservenook.auth.application.RequestPasswordResetService
import com.reservenook.auth.application.ResetPasswordService
import jakarta.servlet.http.HttpSession
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    private val loginService: LoginService,
    private val requestPasswordResetService: RequestPasswordResetService,
    private val resetPasswordService: ResetPasswordService
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
    fun requestPasswordReset(@Valid @RequestBody request: RequestPasswordResetRequest): RequestPasswordResetResponse {
        val result = requestPasswordResetService.request(request.email)
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
            redirectTo = if (principal.isPlatformAdmin) "/platform-admin" else "/app/company/${principal.companySlug}"
        )
    }

    @PostMapping("/api/auth/logout")
    fun logout(session: HttpSession?): LoginResponse {
        session?.invalidate()
        SecurityContextHolder.clearContext()

        return LoginResponse(redirectTo = "/en/login")
    }
}
