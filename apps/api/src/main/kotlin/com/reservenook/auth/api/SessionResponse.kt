package com.reservenook.auth.api

data class SessionResponse(
    val userId: Long,
    val email: String,
    val isPlatformAdmin: Boolean,
    val companySlug: String?,
    val companyRole: String?,
    val redirectTo: String
)
