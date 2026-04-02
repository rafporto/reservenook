package com.reservenook.operations.application

import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.security.application.RequestThrottleService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class OperationsAlertServiceTest {

    private val operationsAlertSender = mockk<OperationsAlertSender>(relaxed = true)
    private val requestThrottleService = mockk<RequestThrottleService>()
    private val brandedEmailTemplateRenderer = BrandedEmailTemplateRenderer(
        RegistrationProperties(publicBaseUrl = "http://localhost:3000")
    )

    @Test
    fun `sends operational alert when enabled and outside cooldown`() {
        val service = OperationsAlertService(
            operationsAlertProperties = OperationsAlertProperties(
                enabled = true,
                recipientEmail = "ops@reservenook.com",
                cooldownMinutes = 30
            ),
            operationsAlertSender = operationsAlertSender,
            requestThrottleService = requestThrottleService,
            brandedEmailTemplateRenderer = brandedEmailTemplateRenderer
        )
        every {
            requestThrottleService.countActiveAttempts("ops-alert", "company-deletion-failure", Duration.ofMinutes(30), any())
        } returns 0
        justRun {
            requestThrottleService.assertAllowed("ops-alert", "company-deletion-failure", 1, Duration.ofMinutes(30), any())
        }

        service.sendAlert(
            scope = "company-deletion-failure",
            title = "Company deletion failed",
            intro = "A scheduled company deletion did not complete successfully.",
            details = mapOf("companySlug" to "acme-wellness", "reason" to "Deletion failed"),
            now = Instant.parse("2026-04-02T10:00:00Z")
        )

        verify(exactly = 1) {
            operationsAlertSender.sendAlert(
                "ops@reservenook.com",
                match {
                    it.subject == "[ReserveNook] Company deletion failed" &&
                        it.actionUrl == "http://localhost:3000/platform-admin" &&
                        it.details["companySlug"] == "acme-wellness"
                }
            )
        }
    }

    @Test
    fun `does not send duplicate alert during cooldown`() {
        val service = OperationsAlertService(
            operationsAlertProperties = OperationsAlertProperties(
                enabled = true,
                recipientEmail = "ops@reservenook.com",
                cooldownMinutes = 30
            ),
            operationsAlertSender = operationsAlertSender,
            requestThrottleService = requestThrottleService,
            brandedEmailTemplateRenderer = brandedEmailTemplateRenderer
        )
        every {
            requestThrottleService.countActiveAttempts("ops-alert", "company-deletion-failure", Duration.ofMinutes(30), any())
        } returns 1

        service.sendAlert(
            scope = "company-deletion-failure",
            title = "Company deletion failed",
            intro = "A scheduled company deletion did not complete successfully.",
            details = mapOf("companySlug" to "acme-wellness")
        )

        verify(exactly = 0) { operationsAlertSender.sendAlert(any(), any()) }
    }

    @Test
    fun `does not send alert when disabled`() {
        val service = OperationsAlertService(
            operationsAlertProperties = OperationsAlertProperties(enabled = false, recipientEmail = "ops@reservenook.com"),
            operationsAlertSender = operationsAlertSender,
            requestThrottleService = requestThrottleService,
            brandedEmailTemplateRenderer = brandedEmailTemplateRenderer
        )

        service.sendAlert(
            scope = "company-deletion-failure",
            title = "Company deletion failed",
            intro = "A scheduled company deletion did not complete successfully.",
            details = mapOf("companySlug" to "acme-wellness")
        )

        verify(exactly = 0) { operationsAlertSender.sendAlert(any(), any()) }
    }
}
