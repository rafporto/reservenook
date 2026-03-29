package com.reservenook.registration.api

import jakarta.validation.constraints.NotBlank

data class ActivateCompanyRequest(
    @field:NotBlank
    val token: String
)
