package com.reservenook.registration.api

import com.reservenook.registration.application.CompanyActivationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/companies/activation")
class CompanyActivationController(
    private val companyActivationService: CompanyActivationService
) {

    @PostMapping("/confirm")
    fun confirmActivation(@Valid @RequestBody request: ActivateCompanyRequest): ActivateCompanyResponse {
        val result = companyActivationService.activate(request.token)

        return ActivateCompanyResponse(
            status = result.outcome,
            message = result.message
        )
    }
}
