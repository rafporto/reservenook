package com.reservenook.registration.application

import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.PlanType

data class RegisterCompanyCommand(
    val companyName: String,
    val businessType: BusinessType,
    val slug: String,
    val email: String,
    val password: String,
    val planType: PlanType,
    val defaultLanguage: String,
    val defaultLocale: String
)
