package com.reservenook.companylifecycle.application

import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companylifecycle.domain.CompanyDeletionEventStatus
import com.reservenook.companylifecycle.infrastructure.CompanyDeletionEventRepository
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

class CompanyDeletionServiceTest {

    private val companyRepository = mockk<CompanyRepository>(relaxed = true)
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>(relaxed = true)
    private val companySubscriptionRepository = mockk<CompanySubscriptionRepository>(relaxed = true)
    private val activationTokenRepository = mockk<ActivationTokenRepository>(relaxed = true)
    private val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
    private val inactivityNotificationEventRepository = mockk<InactivityNotificationEventRepository>(relaxed = true)
    private val companyDeletionEventRepository = mockk<CompanyDeletionEventRepository>(relaxed = true)
    private val userAccountRepository = mockk<UserAccountRepository>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val service = CompanyDeletionService(
        companyRepository = companyRepository,
        companyMembershipRepository = companyMembershipRepository,
        companySubscriptionRepository = companySubscriptionRepository,
        activationTokenRepository = activationTokenRepository,
        passwordResetTokenRepository = passwordResetTokenRepository,
        inactivityNotificationEventRepository = inactivityNotificationEventRepository,
        companyDeletionEventRepository = companyDeletionEventRepository,
        userAccountRepository = userAccountRepository,
        securityAuditService = securityAuditService
    )

    @Test
    fun `deletion candidate is selected only after configured retention period`() {
        val user = UserAccount(
            id = 3L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true
        )
        val company = pendingDeletionCompany(Instant.parse("2026-03-30T12:00:00Z"))
        val membership = CompanyMembership(company = company, user = user, role = CompanyRole.COMPANY_ADMIN)

        every { companyRepository.findAllByStatus(CompanyStatus.PENDING_DELETION) } returns listOf(company)
        every { companyMembershipRepository.findAllByCompanyId(1L) } returns listOf(membership)
        every { companyMembershipRepository.countByUserId(3L) } returns 0L
        every { userAccountRepository.findById(3L) } returns Optional.of(user)
        every { companyDeletionEventRepository.save(any()) } answers { firstArg() }

        val result = service.deletePendingCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.deletedCompanies shouldBe 1
        result.failedDeletions shouldBe 0
        verify(exactly = 1) { companyRepository.delete(company) }
        verify(exactly = 1) {
            companyDeletionEventRepository.save(match { it.status == CompanyDeletionEventStatus.SUCCEEDED && it.companyId == 1L })
        }
    }

    @Test
    fun `reactivated company is removed from deletion candidates`() {
        every { companyRepository.findAllByStatus(CompanyStatus.PENDING_DELETION) } returns emptyList()

        val result = service.deletePendingCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.deletedCompanies shouldBe 0
        verify(exactly = 0) { companyRepository.delete(any()) }
    }

    @Test
    fun `legal hold company is skipped from deletion candidates`() {
        val company = pendingDeletionCompany(Instant.parse("2026-03-30T12:00:00Z")).apply {
            legalHoldUntil = Instant.parse("2026-04-30T12:00:00Z")
        }
        every { companyRepository.findAllByStatus(CompanyStatus.PENDING_DELETION) } returns listOf(company)

        val result = service.deletePendingCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.deletedCompanies shouldBe 0
        verify(exactly = 0) { companyRepository.delete(any()) }
    }

    @Test
    fun `failed deletion is recorded for retry`() {
        val company = pendingDeletionCompany(Instant.parse("2026-03-30T12:00:00Z"))
        val user = UserAccount(
            id = 3L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true
        )
        val membership = CompanyMembership(company = company, user = user, role = CompanyRole.COMPANY_ADMIN)

        every { companyRepository.findAllByStatus(CompanyStatus.PENDING_DELETION) } returns listOf(company)
        every { companyMembershipRepository.findAllByCompanyId(1L) } returns listOf(membership)
        every { companyMembershipRepository.deleteAllByCompanyId(1L) } throws IllegalStateException("Deletion failed")
        every { companyDeletionEventRepository.save(any()) } answers { firstArg() }

        val result = service.deletePendingCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.deletedCompanies shouldBe 0
        result.failedDeletions shouldBe 1
        verify(exactly = 1) {
            companyDeletionEventRepository.save(match {
                it.status == CompanyDeletionEventStatus.FAILED &&
                    it.companyId == 1L &&
                    it.failureReason == "Deletion failed"
            })
        }
        verify(exactly = 1) {
            securityAuditService.record(
                eventType = com.reservenook.security.domain.SecurityAuditEventType.COMPANY_DELETION_FAILED,
                outcome = com.reservenook.security.domain.SecurityAuditOutcome.FAILURE,
                actorUserId = null,
                actorEmail = null,
                companySlug = "acme-wellness",
                targetEmail = null,
                details = "Deletion failed"
            )
        }
    }

    private fun pendingDeletionCompany(deletionScheduledAt: Instant) = Company(
        id = 1L,
        name = "Acme Wellness",
        businessType = BusinessType.APPOINTMENT,
        slug = "acme-wellness",
        status = CompanyStatus.PENDING_DELETION,
        defaultLanguage = "en",
        defaultLocale = "en-US",
        lastActivityAt = Instant.parse("2025-12-01T00:00:00Z"),
        inactiveAt = Instant.parse("2026-01-12T12:00:00Z"),
        deletionScheduledAt = deletionScheduledAt
    )
}
