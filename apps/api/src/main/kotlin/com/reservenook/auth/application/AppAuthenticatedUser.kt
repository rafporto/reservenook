package com.reservenook.auth.application

data class AppAuthenticatedUser(
    val userId: Long,
    val email: String,
    val isPlatformAdmin: Boolean,
    val companySlug: String? = null,
    val passwordVersion: Int = 0
)
