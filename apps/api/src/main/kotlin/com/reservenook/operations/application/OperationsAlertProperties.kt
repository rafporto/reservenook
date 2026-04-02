package com.reservenook.operations.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.operations.alerts")
data class OperationsAlertProperties(
    val enabled: Boolean = false,
    val recipientEmail: String = "",
    val fromEmail: String = "noreply@reservenook.local",
    val cooldownMinutes: Long = 30
)
