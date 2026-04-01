package com.reservenook.auth.application

import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.RequestThrottleService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class RequestPasswordResetServiceTest {

    private val userAccountRepository = mockk<UserAccountRepository>()
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>()
    private val passwordResetMailSender = mockk<PasswordResetMailSender>()
    private val requestThrottleService = mockk<RequestThrottleService>(relaxed = true)
    private val registrationProperties = RegistrationProperties(
        publicBaseUrl = "http://localhost:3000",
        activationTokenHours = 48,
        resendCooldownMinutes = 5,
        passwordResetTokenHours = 2,
        passwordResetCooldownMinutes = 5
    )

    private val service = RequestPasswordResetService(
        userAccountRepository = userAccountRepository,
        companyMembershipRepository = companyMembershipRepository,
        passwordResetTokenRepository = passwordResetTokenRepository,
        passwordResetMailSender = passwordResetMailSender,
        registrationProperties = registrationProperties,
        requestThrottleService = requestThrottleService
    )

    @Test
    fun `request creates token and dispatches reset email for eligible account`() {
        val user = activeUser()
        val existingToken = PasswordResetToken(
            id = 1L,
            token = "old-token",
            user = user,
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now().minusSeconds(600)
        )
        val membership = companyMembership(user, "de")
        val tokenSlot = slot<PasswordResetToken>()

        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { companyMembershipRepository.findFirstByUserId(2L) } returns membership
        every { passwordResetTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(2L) } returns existingToken
        every { passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(2L) } returns listOf(existingToken)
        every { passwordResetTokenRepository.save(capture(tokenSlot)) } answers { firstArg() }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }

        val result = service.request("Admin@Acme.com", "127.0.0.1|admin@acme.com")

        result.message shouldBe "If the account is eligible, a password reset email will be sent."
        (existingToken.usedAt != null) shouldBe true
        verify(exactly = 1) {
            passwordResetMailSender.sendPasswordResetEmail(
                "admin@acme.com",
                match { it.startsWith("http://localhost:3000/de/reset-password?token=") },
                "de"
            )
        }
        tokenSlot.captured.user.id shouldBe 2L
    }

    @Test
    fun `request returns neutral response for unknown account`() {
        every { userAccountRepository.findByEmail("missing@acme.com") } returns null

        val result = service.request("missing@acme.com", "127.0.0.1|missing@acme.com")

        result.message shouldBe "If the account is eligible, a password reset email will be sent."
        verify(exactly = 0) { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
    }

    @Test
    fun `request obeys cooldown and does not send a new email`() {
        val user = activeUser()
        val recentToken = PasswordResetToken(
            id = 1L,
            token = "recent-token",
            user = user,
            expiresAt = Instant.now().plusSeconds(3600),
            createdAt = Instant.now().minusSeconds(60)
        )
        val membership = companyMembership(user, "de")

        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { companyMembershipRepository.findFirstByUserId(2L) } returns membership
        every { passwordResetTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(2L) } returns recentToken

        val result = service.request("admin@acme.com", "127.0.0.1|admin@acme.com")

        result.message shouldBe "If the account is eligible, a password reset email will be sent."
        verify(exactly = 0) { passwordResetTokenRepository.save(any()) }
        verify(exactly = 0) { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
    }

    private fun activeUser() = UserAccount(
        id = 2L,
        email = "admin@acme.com",
        passwordHash = "encoded",
        status = UserStatus.ACTIVE,
        emailVerified = true
    )

    private fun companyMembership(user: UserAccount, language: String) = CompanyMembership(
        company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = language,
            defaultLocale = if (language == "de") "de-DE" else "en-US"
        ),
        user = user,
        role = CompanyRole.COMPANY_ADMIN
    )
}
