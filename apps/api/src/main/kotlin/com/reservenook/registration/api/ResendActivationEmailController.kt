package com.reservenook.registration.api

import com.reservenook.registration.application.ResendActivationEmailService
import com.reservenook.security.application.RequestFingerprintResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/companies/activation")
class ResendActivationEmailController(
    private val resendActivationEmailService: ResendActivationEmailService
) {

    @PostMapping("/resend")
    fun resend(
        @Valid @RequestBody request: ResendActivationEmailRequest,
        httpServletRequest: HttpServletRequest
    ): ResendActivationEmailResponse {
        val result = resendActivationEmailService.resend(
            request.email,
            RequestFingerprintResolver.resolve(httpServletRequest, request.email)
        )
        return ResendActivationEmailResponse(result.message)
    }
}
