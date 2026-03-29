package com.reservenook.registration.application

import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.Instant

class CompanyActivationServiceTest {

    private val activationTokenRepository = mockk<ActivationTokenRepository>()
    private val service = CompanyActivationService(activationTokenRepository)

    @Test
    fun `activate marks company user and token as active`() {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.PENDING_ACTIVATION,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val user = UserAccount(
            id = 2L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = UserStatus.PENDING_ACTIVATION,
            emailVerified = false
        )
        val token = ActivationToken(
            id = 3L,
            token = "valid-token",
            company = company,
            user = user,
            expiresAt = Instant.now().plusSeconds(3600)
        )

        every { activationTokenRepository.findByToken("valid-token") } returns token

        val result = service.activate("valid-token")

        result.outcome shouldBe ActivationOutcome.ACTIVATED
        company.status shouldBe CompanyStatus.ACTIVE
        user.status shouldBe UserStatus.ACTIVE
        user.emailVerified shouldBe true
        (token.usedAt != null) shouldBe true
    }

    @Test
    fun `activate rejects expired token`() {
        val token = activationToken(
            token = "expired-token",
            expiresAt = Instant.now().minusSeconds(60)
        )

        every { activationTokenRepository.findByToken("expired-token") } returns token

        val result = service.activate("expired-token")

        result.outcome shouldBe ActivationOutcome.EXPIRED
    }

    @Test
    fun `activate returns already active for reused token`() {
        val token = activationToken(
            token = "used-token",
            usedAt = Instant.now().minusSeconds(10),
            companyStatus = CompanyStatus.ACTIVE,
            userStatus = UserStatus.ACTIVE,
            emailVerified = true
        )

        every { activationTokenRepository.findByToken("used-token") } returns token

        val result = service.activate("used-token")

        result.outcome shouldBe ActivationOutcome.ALREADY_ACTIVE
    }

    @Test
    fun `activate rejects unknown token`() {
        every { activationTokenRepository.findByToken("missing-token") } returns null

        val result = service.activate("missing-token")

        result.outcome shouldBe ActivationOutcome.INVALID
    }

    private fun activationToken(
        token: String,
        expiresAt: Instant = Instant.now().plusSeconds(3600),
        usedAt: Instant? = null,
        companyStatus: CompanyStatus = CompanyStatus.PENDING_ACTIVATION,
        userStatus: UserStatus = UserStatus.PENDING_ACTIVATION,
        emailVerified: Boolean = false
    ): ActivationToken {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = companyStatus,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val user = UserAccount(
            id = 2L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = userStatus,
            emailVerified = emailVerified
        )

        return ActivationToken(
            id = 3L,
            token = token,
            company = company,
            user = user,
            expiresAt = expiresAt,
            usedAt = usedAt
        )
    }
}
