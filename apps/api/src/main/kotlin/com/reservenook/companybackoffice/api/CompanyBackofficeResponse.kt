package com.reservenook.companybackoffice.api

data class CompanyBackofficeResponse(
    val company: CompanyBackofficeCompanySummary,
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
