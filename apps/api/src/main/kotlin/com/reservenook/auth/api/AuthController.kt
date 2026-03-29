package com.reservenook.auth.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.LoginService
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
    private val loginService: LoginService
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
