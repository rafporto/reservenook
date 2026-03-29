package com.reservenook.companybackoffice.api

data class CompanyBackofficeResponse(
    val companyName: String,
    val companySlug: String,
    val businessType: String,
    val role: String,
    val currentUserEmail: String
)
