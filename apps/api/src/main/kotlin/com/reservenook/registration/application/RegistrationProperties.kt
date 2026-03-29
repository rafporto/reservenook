package com.reservenook.registration.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.registration")
data class RegistrationProperties(
    val publicBaseUrl: String = "http://localhost:3000",
    val activationTokenHours: Long = 48,
    val resendCooldownMinutes: Long = 5,
    val passwordResetTokenHours: Long = 2,
    val passwordResetCooldownMinutes: Long = 5
)
