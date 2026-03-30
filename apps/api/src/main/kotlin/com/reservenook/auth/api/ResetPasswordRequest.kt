package com.reservenook.auth.api

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,

    @field:NotBlank
    @field:Size(min = 8, max = 255)
    val password: String
)
