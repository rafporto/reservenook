package com.reservenook.platformadmin.api

data class InactivityPolicyResponse(
    val inactivityThresholdDays: Int,
    val deletionWarningLeadDays: Int,
    val updatedAt: String
)
