package com.reservenook.platformadmin.api

import jakarta.validation.constraints.Min

data class UpdateInactivityPolicyRequest(
    @field:Min(1)
    val inactivityThresholdDays: Int,

    @field:Min(1)
    val deletionWarningLeadDays: Int
)
