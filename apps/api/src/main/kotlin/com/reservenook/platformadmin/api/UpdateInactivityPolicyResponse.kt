package com.reservenook.platformadmin.api

data class UpdateInactivityPolicyResponse(
    val message: String,
    val policy: InactivityPolicyResponse
)
