package com.reservenook.registration.api

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResendActivationEmailRequest(
    @field:NotBlank
    @field:Email
    @field:Size(max = 255)
    val email: String
)
