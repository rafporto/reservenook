package com.reservenook.registration.application

import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanySubscription
import com.reservenook.registration.domain.PlanType
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class CompanyRegistrationServiceTest {

    private val companyRepository = mockk<CompanyRepository>()
    private val userAccountRepository = mockk<UserAccountRepository>()
    private val membershipRepository = mockk<CompanyMembershipRepository>()
    private val subscriptionRepository = mockk<CompanySubscriptionRepository>()
    private val activationTokenRepository = mockk<ActivationTokenRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val mailSender = mockk<RegistrationMailSender>()
    private val registrationProperties = RegistrationProperties(
        publicBaseUrl = "http://localhost:3000",
        activationTokenHours = 48
    )

    private val service = CompanyRegistrationService(
        companyRepository = companyRepository,
        userAccountRepository = userAccountRepository,
        companyMembershipRepository = membershipRepository,
        companySubscriptionRepository = subscriptionRepository,
        activationTokenRepository = activationTokenRepository,
        passwordEncoder = passwordEncoder,
        registrationMailSender = mailSender,
        registrationProperties = registrationProperties
    )

    @Test
    fun `register creates pending activation entities and activation link`() {
        val membershipSlot = slot<CompanyMembership>()
        val subscriptionSlot = slot<CompanySubscription>()
        val userSlot = slot<UserAccount>()

        every { companyRepository.existsBySlug("acme-wellness") } returns false
        every { userAccountRepository.existsByEmail("admin@acme.com") } returns false
        every { passwordEncoder.encode("SecurePass123") } returns "encoded-password"
        every { companyRepository.save(any()) } answers {
            firstArg<Company>().apply { id = 1L }
        }
        every { userAccountRepository.save(capture(userSlot)) } answers { firstArg() }
        every { membershipRepository.save(capture(membershipSlot)) } answers { firstArg() }
        every { subscriptionRepository.save(capture(subscriptionSlot)) } answers { firstArg() }
        every { activationTokenRepository.save(any()) } answers { firstArg() }
        justRun { mailSender.sendActivationEmail(any(), any()) }

        service.register(
            RegisterCompanyCommand(
                companyName = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "Acme-Wellness",
                email = "Admin@Acme.com",
                password = "SecurePass123",
                planType = PlanType.TRIAL,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        userSlot.captured.email shouldBe "admin@acme.com"
        userSlot.captured.passwordHash shouldBe "encoded-password"
        membershipSlot.captured.role.name shouldBe "COMPANY_ADMIN"
        subscriptionSlot.captured.planType shouldBe PlanType.TRIAL
        verify(exactly = 1) {
            mailSender.sendActivationEmail(
                "admin@acme.com",
                match { it.startsWith("http://localhost:3000/en/activate?token=") }
            )
        }
    }
}
