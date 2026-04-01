package com.reservenook.companylifecycle.application

import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

class CompanyInactivityEvaluationServiceTest {

    private val companyRepository = mockk<CompanyRepository>(relaxed = true)
    private val inactivityPolicyRepository = mockk<InactivityPolicyRepository>()
    private val companyInactivityNotificationService = mockk<CompanyInactivityNotificationService>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val service = CompanyInactivityEvaluationService(
        companyRepository,
        inactivityPolicyRepository,
        companyInactivityNotificationService,
        securityAuditService
    )

    @Test
    fun `company becomes inactive after configured threshold`() {
        val now = Instant.parse("2026-03-30T12:00:00Z")
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US",
            lastActivityAt = Instant.parse("2025-12-01T00:00:00Z")
        )

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.ACTIVE) } returns listOf(company)

        val result = service.evaluate(now)

        result.companiesMarkedInactive shouldBe 1
        company.status shouldBe CompanyStatus.INACTIVE
        company.inactiveAt shouldBe now
        company.deletionScheduledAt shouldBe Instant.parse("2026-06-28T12:00:00Z")
        verify(exactly = 1) { companyInactivityNotificationService.notifyCompanies(listOf(company), now) }
    }

    @Test
    fun `active company is not marked inactive prematurely`() {
        val now = Instant.parse("2026-03-30T12:00:00Z")
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US",
            lastActivityAt = Instant.parse("2026-03-15T00:00:00Z")
        )

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.ACTIVE) } returns listOf(company)

        val result = service.evaluate(now)

        result.companiesMarkedInactive shouldBe 0
        company.status shouldBe CompanyStatus.ACTIVE
        verify(exactly = 1) { companyInactivityNotificationService.notifyCompanies(emptyList(), now) }
    }

    @Test
    fun `recently reactivated company is not marked inactive incorrectly`() {
        val now = Instant.parse("2026-03-30T12:00:00Z")
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US",
            lastActivityAt = Instant.parse("2026-03-29T00:00:00Z"),
            inactiveAt = Instant.parse("2026-02-01T00:00:00Z")
        )

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.ACTIVE) } returns listOf(company)

        val result = service.evaluate(now)

        result.companiesMarkedInactive shouldBe 0
        company.status shouldBe CompanyStatus.ACTIVE
        verify(exactly = 1) { companyInactivityNotificationService.notifyCompanies(emptyList(), now) }
    }
}
