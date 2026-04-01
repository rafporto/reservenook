package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.Optional

class PlatformInactivityPolicyServiceTest {

    private val inactivityPolicyRepository = mockk<InactivityPolicyRepository>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val service = PlatformInactivityPolicyService(inactivityPolicyRepository, securityAuditService)

    @Test
    fun `platform admin reads current inactivity policy`() {
        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(
                id = 1L,
                inactivityThresholdDays = 90,
                deletionWarningLeadDays = 14,
                updatedAt = Instant.parse("2026-03-30T10:00:00Z")
            )
        )

        val result = service.getPolicy(
            AppAuthenticatedUser(userId = 1L, email = "platform@reservenook.com", isPlatformAdmin = true)
        )

        result.inactivityThresholdDays shouldBe 90
        result.deletionWarningLeadDays shouldBe 14
    }

    @Test
    fun `platform admin updates inactivity policy`() {
        val policy = InactivityPolicy(
            id = 1L,
            inactivityThresholdDays = 90,
            deletionWarningLeadDays = 14,
            updatedAt = Instant.parse("2026-03-30T10:00:00Z")
        )
        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(policy)

        val result = service.updatePolicy(
            principal = AppAuthenticatedUser(userId = 1L, email = "platform@reservenook.com", isPlatformAdmin = true),
            inactivityThresholdDays = 120,
            deletionWarningLeadDays = 21
        )

        result.inactivityThresholdDays shouldBe 120
        result.deletionWarningLeadDays shouldBe 21
    }

    @Test
    fun `warning lead time greater than inactivity threshold is rejected`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.updatePolicy(
                principal = AppAuthenticatedUser(userId = 1L, email = "platform@reservenook.com", isPlatformAdmin = true),
                inactivityThresholdDays = 30,
                deletionWarningLeadDays = 45
            )
        }

        exception.message shouldBe "Deletion warning lead time cannot be greater than the inactivity threshold."
    }

    @Test
    fun `non platform user cannot update inactivity policy`() {
        val exception = assertThrows<ResponseStatusException> {
            service.updatePolicy(
                principal = AppAuthenticatedUser(
                    userId = 2L,
                    email = "admin@acme.com",
                    isPlatformAdmin = false,
                    companySlug = "acme-wellness"
                ),
                inactivityThresholdDays = 60,
                deletionWarningLeadDays = 14
            )
        }

        exception.statusCode.value() shouldBe 403
    }
}
