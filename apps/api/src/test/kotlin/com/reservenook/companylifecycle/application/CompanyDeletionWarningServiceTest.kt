package com.reservenook.companylifecycle.application

import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mail.MailSendException
import java.time.Instant
import java.util.Optional

class CompanyDeletionWarningServiceTest {

    private val companyRepository = mockk<CompanyRepository>()
    private val inactivityPolicyRepository = mockk<InactivityPolicyRepository>()
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val inactivityNotificationEventRepository = mockk<InactivityNotificationEventRepository>()
    private val companyDeletionWarningMailSender = mockk<CompanyDeletionWarningMailSender>()
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val service = CompanyDeletionWarningService(
        companyRepository = companyRepository,
        inactivityPolicyRepository = inactivityPolicyRepository,
        companyMembershipRepository = companyMembershipRepository,
        inactivityNotificationEventRepository = inactivityNotificationEventRepository,
        companyDeletionWarningMailSender = companyDeletionWarningMailSender,
        securityAuditService = securityAuditService
    )

    @Test
    fun `warning trigger fires at configured lead time`() {
        val company = inactiveCompany(deletionScheduledAt = Instant.parse("2026-04-12T12:00:00Z"))
        val recipient = recipient(company)

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.INACTIVE) } returns listOf(company)
        every {
            inactivityNotificationEventRepository.existsByCompanyIdAndNotificationTypeAndStatus(
                1L,
                CompanyLifecycleNotificationType.DELETION_WARNING,
                InactivityNotificationStatus.SENT
            )
        } returns false
        every { companyMembershipRepository.findAllByCompanyIdAndRole(1L, CompanyRole.COMPANY_ADMIN) } returns listOf(recipient)
        every { inactivityNotificationEventRepository.save(any()) } answers { firstArg() }
        justRun {
            companyDeletionWarningMailSender.sendDeletionWarningEmail(
                "admin@acme.com",
                "Acme Wellness",
                Instant.parse("2026-04-12T12:00:00Z"),
                "en"
            )
        }

        val result = service.warnPendingDeletionCompanies(Instant.parse("2026-03-29T12:00:00Z"))

        result.warningsSent shouldBe 1
        result.failedWarnings shouldBe 0
        company.status shouldBe CompanyStatus.PENDING_DELETION
        verify(exactly = 1) {
            companyDeletionWarningMailSender.sendDeletionWarningEmail(
                "admin@acme.com",
                "Acme Wellness",
                Instant.parse("2026-04-12T12:00:00Z"),
                "en"
            )
        }
    }

    @Test
    fun `warning is not sent too early`() {
        val company = inactiveCompany(deletionScheduledAt = Instant.parse("2026-04-20T12:00:00Z"))

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.INACTIVE) } returns listOf(company)

        val result = service.warnPendingDeletionCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.warningsSent shouldBe 0
        verify(exactly = 0) { companyMembershipRepository.findAllByCompanyIdAndRole(any(), any()) }
    }

    @Test
    fun `duplicate warning is prevented by default`() {
        val company = inactiveCompany(deletionScheduledAt = Instant.parse("2026-04-12T12:00:00Z"))

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.INACTIVE) } returns listOf(company)
        every {
            inactivityNotificationEventRepository.existsByCompanyIdAndNotificationTypeAndStatus(
                1L,
                CompanyLifecycleNotificationType.DELETION_WARNING,
                InactivityNotificationStatus.SENT
            )
        } returns true

        val result = service.warnPendingDeletionCompanies(Instant.parse("2026-03-29T12:00:00Z"))

        result.warningsSent shouldBe 0
        verify(exactly = 0) { companyMembershipRepository.findAllByCompanyIdAndRole(any(), any()) }
    }

    @Test
    fun `reactivated company does not receive stale deletion warning`() {
        val company = inactiveCompany(
            status = CompanyStatus.ACTIVE,
            deletionScheduledAt = Instant.parse("2026-04-12T12:00:00Z")
        )

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.INACTIVE) } returns emptyList()

        val result = service.warnPendingDeletionCompanies(Instant.parse("2026-03-29T12:00:00Z"))

        result.warningsSent shouldBe 0
        verify(exactly = 0) { companyDeletionWarningMailSender.sendDeletionWarningEmail(any(), any(), any(), any()) }
    }

    @Test
    fun `warning delivery failure is recorded for retry or review`() {
        val company = inactiveCompany(deletionScheduledAt = Instant.parse("2026-04-12T12:00:00Z"))
        val recipient = recipient(company)

        every { inactivityPolicyRepository.findById(1L) } returns Optional.of(
            InactivityPolicy(id = 1L, inactivityThresholdDays = 90, deletionWarningLeadDays = 14)
        )
        every { companyRepository.findAllByStatus(CompanyStatus.INACTIVE) } returns listOf(company)
        every {
            inactivityNotificationEventRepository.existsByCompanyIdAndNotificationTypeAndStatus(
                1L,
                CompanyLifecycleNotificationType.DELETION_WARNING,
                InactivityNotificationStatus.SENT
            )
        } returns false
        every { companyMembershipRepository.findAllByCompanyIdAndRole(1L, CompanyRole.COMPANY_ADMIN) } returns listOf(recipient)
        every { inactivityNotificationEventRepository.save(any()) } answers { firstArg() }
        every {
            companyDeletionWarningMailSender.sendDeletionWarningEmail(
                "admin@acme.com",
                "Acme Wellness",
                Instant.parse("2026-04-12T12:00:00Z"),
                "en"
            )
        } throws MailSendException("SMTP unavailable")

        val result = service.warnPendingDeletionCompanies(Instant.parse("2026-03-29T12:00:00Z"))

        result.warningsSent shouldBe 0
        result.failedWarnings shouldBe 1
    }

    private fun inactiveCompany(
        status: CompanyStatus = CompanyStatus.INACTIVE,
        deletionScheduledAt: Instant
    ) = Company(
        id = 1L,
        name = "Acme Wellness",
        businessType = BusinessType.APPOINTMENT,
        slug = "acme-wellness",
        status = status,
        defaultLanguage = "en",
        defaultLocale = "en-US",
        lastActivityAt = Instant.parse("2025-12-01T00:00:00Z"),
        inactiveAt = Instant.parse("2026-01-12T12:00:00Z"),
        deletionScheduledAt = deletionScheduledAt
    )

    private fun recipient(company: Company) = CompanyMembership(
        company = company,
        user = UserAccount(
            id = 3L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true
        ),
        role = CompanyRole.COMPANY_ADMIN
    )
}
