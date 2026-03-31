package com.reservenook.companylifecycle.application

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.application.RegistrationMailSender
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class CompanyInactivityNotificationServiceIntegrationTest(
    @Autowired private val evaluationService: CompanyInactivityEvaluationService,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val inactivityPolicyRepository: InactivityPolicyRepository,
    @Autowired private val inactivityNotificationEventRepository: InactivityNotificationEventRepository
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
        inactivityNotificationEventRepository.deleteAll()
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
    fun `notification dispatch occurs when inactivity state is entered`() {
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
        val user = userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = "encoded",
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )
        membershipRepository.save(
            CompanyMembership(
                company = company,
                user = user,
                role = CompanyRole.COMPANY_ADMIN
            )
        )

        val result = evaluationService.evaluate(Instant.parse("2026-03-30T12:00:00Z"))

        result.companiesMarkedInactive shouldBe 1
        verify(exactly = 1) { companyInactivityMailSender.sendInactivityEmail("admin@acme.com", "Acme Wellness", "en") }
        inactivityNotificationEventRepository.findAllByCompanyId(requireNotNull(company.id)).shouldHaveSize(1)
        inactivityNotificationEventRepository.findAllByCompanyId(requireNotNull(company.id)).single().status shouldBe InactivityNotificationStatus.SENT
        inactivityNotificationEventRepository.findAllByCompanyId(requireNotNull(company.id)).single().notificationType shouldBe CompanyLifecycleNotificationType.INACTIVITY_NOTICE
    }
}
