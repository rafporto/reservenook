package com.reservenook.auth.api

import com.reservenook.auth.application.LoginService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/auth")
class AuthController(
    private val loginService: LoginService
) {

    @PostMapping("/login")
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
}
