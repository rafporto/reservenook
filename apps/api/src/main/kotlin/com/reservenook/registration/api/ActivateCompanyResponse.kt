package com.reservenook.registration.api

import com.reservenook.registration.application.ActivationOutcome

data class ActivateCompanyResponse(
    val status: ActivationOutcome,
    val message: String
)
