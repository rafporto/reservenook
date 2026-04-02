package com.reservenook.security.application

import com.reservenook.operations.application.OperationsAlertService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class SecurityAuditServiceTest {

    private val securityAuditEventRepository = mockk<SecurityAuditEventRepository>(relaxed = true)
    private val operationsAlertService = mockk<OperationsAlertService>(relaxed = true)
    private val service = SecurityAuditService(securityAuditEventRepository, operationsAlertService)

    @Test
    fun `critical failure audit triggers operational alert`() {
        every { securityAuditEventRepository.save(any()) } answers { firstArg() }
        justRun {
            operationsAlertService.sendAlert(
                scope = any(),
                title = any(),
                intro = any(),
                details = any(),
                now = any()
            )
        }

        service.record(
            eventType = SecurityAuditEventType.COMPANY_DELETION_FAILED,
            outcome = SecurityAuditOutcome.FAILURE,
            companySlug = "acme-wellness",
            details = "Deletion failed"
        )

        verify(exactly = 1) {
            operationsAlertService.sendAlert(
                scope = "audit-failure-company_deletion_failed",
                title = "Company Deletion Failed alert",
                intro = "A critical operational failure was recorded by ReserveNook.",
                details = match {
                    it["companySlug"] == "acme-wellness" &&
                        it["details"] == "Deletion failed"
                },
                now = any()
            )
        }
    }

    @Test
    fun `successful audit does not trigger operational alert`() {
        every { securityAuditEventRepository.save(any()) } answers { firstArg() }

        service.record(
            eventType = SecurityAuditEventType.COMPANY_DELETED,
            outcome = SecurityAuditOutcome.SUCCESS,
            companySlug = "acme-wellness"
        )

        verify(exactly = 0) { operationsAlertService.sendAlert(any(), any(), any(), any(), any()) }
    }
}
