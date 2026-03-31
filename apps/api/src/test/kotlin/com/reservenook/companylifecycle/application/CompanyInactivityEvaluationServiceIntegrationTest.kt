package com.reservenook.companylifecycle.application

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.application.RegistrationMailSender
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class CompanyInactivityEvaluationServiceIntegrationTest(
    @Autowired private val service: CompanyInactivityEvaluationService,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val inactivityPolicyRepository: InactivityPolicyRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val userAccountRepository: UserAccountRepository
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @MockkBean
    private lateinit var companyInactivityMailSender: CompanyInactivityMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
        justRun { companyInactivityMailSender.sendInactivityEmail(any(), any(), any()) }
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
        inactivityPolicyRepository.deleteAll()
        inactivityPolicyRepository.save(
            InactivityPolicy(
                id = 1L,
                inactivityThresholdDays = 90,
                deletionWarningLeadDays = 14
            )
        )
    }

    @Test
    fun `scheduled inactivity evaluation updates company state and timestamps`() {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-wellness",
                status = CompanyStatus.ACTIVE,
                defaultLanguage = "en",
                defaultLocale = "en-US",
                lastActivityAt = Instant.parse("2025-12-01T00:00:00Z")
            )
        )

        val result = service.evaluate(Instant.parse("2026-03-30T12:00:00Z"))

        result.companiesMarkedInactive shouldBe 1

        val updatedCompany = companyRepository.findById(requireNotNull(company.id)).orElseThrow()
        updatedCompany.status shouldBe CompanyStatus.INACTIVE
        updatedCompany.inactiveAt shouldBe Instant.parse("2026-03-30T12:00:00Z")
        updatedCompany.deletionScheduledAt shouldBe Instant.parse("2026-06-28T12:00:00Z")
    }
}
