package com.reservenook.platformadmin.api

data class PlatformAdminCompanySummaryResponse(
    val companyName: String,
    val companySlug: String,
    val businessType: String,
    val activationStatus: String,
    val planType: String,
    val expiresAt: String,
    val legalHoldUntil: String?
)
