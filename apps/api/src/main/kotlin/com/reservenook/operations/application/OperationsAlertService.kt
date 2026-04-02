package com.reservenook.operations.application

import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.security.application.RequestThrottleService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class OperationsAlertService(
    private val operationsAlertProperties: OperationsAlertProperties,
    private val operationsAlertSender: OperationsAlertSender,
    private val requestThrottleService: RequestThrottleService,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
) {

    fun sendAlert(
        scope: String,
        title: String,
        intro: String,
        details: Map<String, String>,
        now: Instant = Instant.now()
    ) {
        val recipientEmail = operationsAlertProperties.recipientEmail.trim()
        if (!operationsAlertProperties.enabled || recipientEmail.isBlank()) {
            return
        }

        val cooldownWindow = Duration.ofMinutes(operationsAlertProperties.cooldownMinutes)
        if (requestThrottleService.countActiveAttempts("ops-alert", scope, cooldownWindow, now) > 0) {
            return
        }

        requestThrottleService.assertAllowed("ops-alert", scope, 1, cooldownWindow, now)
        operationsAlertSender.sendAlert(
            recipientEmail = recipientEmail,
            message = OperationsAlertMessage(
                subject = "[ReserveNook] $title",
                title = title,
                intro = intro,
                details = details,
                actionLabel = "Open platform admin",
                actionUrl = "${brandedEmailTemplateRenderer.publicBaseUrl}/platform-admin"
            )
        )
    }

    fun isAlertingEnabled(): Boolean =
        operationsAlertProperties.enabled && operationsAlertProperties.recipientEmail.isNotBlank()

    fun configuredRecipient(): String? =
        operationsAlertProperties.recipientEmail.takeIf { it.isNotBlank() }
}
