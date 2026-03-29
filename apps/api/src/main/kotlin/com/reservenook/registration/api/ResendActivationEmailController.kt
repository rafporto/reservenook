package com.reservenook.registration.api

import com.reservenook.registration.application.ResendActivationEmailService
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
    fun resend(@Valid @RequestBody request: ResendActivationEmailRequest): ResendActivationEmailResponse {
        val result = resendActivationEmailService.resend(request.email)
        return ResendActivationEmailResponse(result.message)
    }
}
