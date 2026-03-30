package com.reservenook.companylifecycle.application

import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.mail.MailSendException
import java.time.Instant

class CompanyInactivityNotificationServiceTest {

    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val inactivityNotificationEventRepository = mockk<InactivityNotificationEventRepository>(relaxed = true)
    private val companyInactivityMailSender = mockk<CompanyInactivityMailSender>()
    private val service = CompanyInactivityNotificationService(
        companyMembershipRepository = companyMembershipRepository,
        inactivityNotificationEventRepository = inactivityNotificationEventRepository,
        companyInactivityMailSender = companyInactivityMailSender
    )

    @Test
    fun `inactive company notification is generated for correct recipients`() {
        val company = inactiveCompany()
        val recipient = CompanyMembership(
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

        every { inactivityNotificationEventRepository.existsByCompanyIdAndStatus(1L, InactivityNotificationStatus.SENT) } returns false
        every { companyMembershipRepository.findAllByCompanyIdAndRole(1L, CompanyRole.COMPANY_ADMIN) } returns listOf(recipient)
        every { inactivityNotificationEventRepository.save(any()) } answers { firstArg() }
        justRun { companyInactivityMailSender.sendInactivityEmail("admin@acme.com", "Acme Wellness") }

        val result = service.notifyCompanies(listOf(company), Instant.parse("2026-03-30T12:00:00Z"))

        result.companiesNotified shouldBe 1
        result.failedNotifications shouldBe 0
        verify(exactly = 1) { companyInactivityMailSender.sendInactivityEmail("admin@acme.com", "Acme Wellness") }
    }

    @Test
    fun `duplicate inactivity notification is prevented after a sent event exists`() {
        val company = inactiveCompany()

        every { inactivityNotificationEventRepository.existsByCompanyIdAndStatus(1L, InactivityNotificationStatus.SENT) } returns true

        val result = service.notifyCompanies(listOf(company), Instant.parse("2026-03-30T12:00:00Z"))

        result.companiesNotified shouldBe 0
        verify(exactly = 0) { companyMembershipRepository.findAllByCompanyIdAndRole(any(), any()) }
    }

    @Test
    fun `delivery failure is recorded for retry or review`() {
        val company = inactiveCompany()
        val recipient = CompanyMembership(
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

        every { inactivityNotificationEventRepository.existsByCompanyIdAndStatus(1L, InactivityNotificationStatus.SENT) } returns false
        every { companyMembershipRepository.findAllByCompanyIdAndRole(1L, CompanyRole.COMPANY_ADMIN) } returns listOf(recipient)
        every { inactivityNotificationEventRepository.save(any()) } answers { firstArg() }
        every { companyInactivityMailSender.sendInactivityEmail("admin@acme.com", "Acme Wellness") } throws MailSendException("SMTP unavailable")

        val result = service.notifyCompanies(listOf(company), Instant.parse("2026-03-30T12:00:00Z"))

        result.companiesNotified shouldBe 0
        result.failedNotifications shouldBe 1
    }

    private fun inactiveCompany() = Company(
        id = 1L,
        name = "Acme Wellness",
        businessType = BusinessType.APPOINTMENT,
        slug = "acme-wellness",
        status = CompanyStatus.INACTIVE,
        defaultLanguage = "en",
        defaultLocale = "en-US",
        lastActivityAt = Instant.parse("2025-12-01T00:00:00Z"),
        inactiveAt = Instant.parse("2026-03-30T12:00:00Z")
    )
}
