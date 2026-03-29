package com.reservenook.registration.api

import com.reservenook.registration.application.CompanyRegistrationService
import com.reservenook.registration.application.RegisterCompanyCommand
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/companies")
class CompanyRegistrationController(
    private val companyRegistrationService: CompanyRegistrationService
) {

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterCompanyRequest): RegisterCompanyResponse {
        companyRegistrationService.register(
            RegisterCompanyCommand(
                companyName = request.companyName,
                businessType = request.businessType,
                slug = request.slug,
                email = request.email,
                password = request.password,
                planType = request.planType,
                defaultLanguage = request.defaultLanguage,
                defaultLocale = request.defaultLocale
            )
        )

        return RegisterCompanyResponse("Registration received. Check your email to activate your account.")
    }
}
