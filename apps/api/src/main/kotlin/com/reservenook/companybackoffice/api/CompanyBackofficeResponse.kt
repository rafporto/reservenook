package com.reservenook.companybackoffice.api

data class CompanyBackofficeResponse(
    val company: CompanyBackofficeCompanySummary,
    val profile: CompanyBackofficeProfileSummary,
    val viewer: CompanyBackofficeViewerSummary,
    val operations: CompanyBackofficeOperationsSummary,
    val configurationAreas: List<CompanyBackofficeAreaSummary>
)

data class CompanyBackofficeCompanySummary(
    val companyName: String,
    val companySlug: String,
    val businessType: String,
    val companyStatus: String,
    val defaultLanguage: String,
    val defaultLocale: String,
    val createdAt: String
)

data class CompanyBackofficeProfileSummary(
    val businessDescription: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val postalCode: String?,
    val countryCode: String?
)

data class CompanyBackofficeViewerSummary(
    val role: String,
    val currentUserEmail: String
)

data class CompanyBackofficeOperationsSummary(
    val planType: String,
    val subscriptionExpiresAt: String?,
    val staffCount: Int,
    val adminCount: Int,
    val lastActivityAt: String,
    val deletionScheduledAt: String?
)

data class CompanyBackofficeAreaSummary(
    val key: String,
    val title: String,
    val description: String,
    val status: String
)

data class UpdateCompanyProfileRequest(
    val companyName: String,
    val businessDescription: String?,
    val contactEmail: String,
    val contactPhone: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val postalCode: String,
    val countryCode: String
)

data class UpdateCompanyProfileResponse(
    val message: String,
    val company: CompanyBackofficeCompanySummary,
    val profile: CompanyBackofficeProfileSummary
)
