package com.reservenook.registration.api

import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.PlanType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterCompanyRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val companyName: String,

    val businessType: BusinessType,

    @field:NotBlank
    @field:Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$")
    val slug: String,

    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 255)
    val password: String,

    val planType: PlanType,

    @field:NotBlank
    @field:Pattern(regexp = "^(en|de|pt)$")
    val defaultLanguage: String,

    @field:NotBlank
    @field:Size(max = 32)
    val defaultLocale: String
)
