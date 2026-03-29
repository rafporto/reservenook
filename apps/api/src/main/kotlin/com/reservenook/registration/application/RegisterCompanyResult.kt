package com.reservenook.registration.application

data class RegisterCompanyResult(
    val companyId: Long,
    val activationToken: String
)
