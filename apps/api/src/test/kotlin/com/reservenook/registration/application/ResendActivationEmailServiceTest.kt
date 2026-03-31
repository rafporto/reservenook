package com.reservenook.registration.application

import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class ResendActivationEmailServiceTest {

    private val userAccountRepository = mockk<UserAccountRepository>()
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val activationTokenRepository = mockk<ActivationTokenRepository>()
    private val registrationMailSender = mockk<RegistrationMailSender>()
    private val registrationProperties = RegistrationProperties(
        publicBaseUrl = "http://localhost:3000",
        activationTokenHours = 48,
        resendCooldownMinutes = 5
    )

    private val service = ResendActivationEmailService(
        userAccountRepository = userAccountRepository,
        companyMembershipRepository = companyMembershipRepository,
        activationTokenRepository = activationTokenRepository,
        registrationMailSender = registrationMailSender,
        registrationProperties = registrationProperties
    )

    @Test
    fun `resend rotates token and dispatches activation email for eligible account`() {
        val user = pendingUser()
        val membership = pendingMembership(user)
        val existingToken = activationToken(user, membership.company, "old-token", Instant.now().minusSeconds(600))
        val newTokenSlot = slot<ActivationToken>()

        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { companyMembershipRepository.findFirstByUserId(2L) } returns membership
        every { activationTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(2L) } returns existingToken
        every { activationTokenRepository.findAllByUserIdAndUsedAtIsNull(2L) } returns listOf(existingToken)
        every { activationTokenRepository.save(capture(newTokenSlot)) } answers { firstArg() }
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }

        val result = service.resend("Admin@Acme.com")

        result.message shouldBe "If the account is pending activation, a new activation email will be sent."
        (existingToken.usedAt != null) shouldBe true
        verify(exactly = 1) {
            registrationMailSender.sendActivationEmail(
                "admin@acme.com",
                match { it.startsWith("http://localhost:3000/en/activate?token=") },
                "en"
            )
        }
        newTokenSlot.captured.user.id shouldBe 2L
    }

    @Test
    fun `resend returns neutral response for unknown email`() {
        every { userAccountRepository.findByEmail("unknown@acme.com") } returns null

        val result = service.resend("unknown@acme.com")

        result.message shouldBe "If the account is pending activation, a new activation email will be sent."
        verify(exactly = 0) { registrationMailSender.sendActivationEmail(any(), any(), any()) }
    }

    @Test
    fun `resend obeys cooldown and does not send a new email`() {
        val user = pendingUser()
        val membership = pendingMembership(user)
        val recentToken = activationToken(user, membership.company, "recent-token", Instant.now().minusSeconds(60))

        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { companyMembershipRepository.findFirstByUserId(2L) } returns membership
        every { activationTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(2L) } returns recentToken

        val result = service.resend("admin@acme.com")

        result.message shouldBe "If the account is pending activation, a new activation email will be sent."
        verify(exactly = 0) { activationTokenRepository.save(any()) }
        verify(exactly = 0) { registrationMailSender.sendActivationEmail(any(), any(), any()) }
    }

    private fun pendingUser() = UserAccount(
        id = 2L,
        email = "admin@acme.com",
        passwordHash = "encoded",
        status = UserStatus.PENDING_ACTIVATION,
        emailVerified = false
    )

    private fun pendingMembership(user: UserAccount): CompanyMembership {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.PENDING_ACTIVATION,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )

        return CompanyMembership(
            id = 3L,
            company = company,
            user = user,
            role = CompanyRole.COMPANY_ADMIN
        )
    }

    private fun activationToken(user: UserAccount, company: Company, tokenValue: String, createdAt: Instant) =
        ActivationToken(
            id = 4L,
            token = tokenValue,
            company = company,
            user = user,
            expiresAt = createdAt.plusSeconds(3600),
            createdAt = createdAt
        )
}
