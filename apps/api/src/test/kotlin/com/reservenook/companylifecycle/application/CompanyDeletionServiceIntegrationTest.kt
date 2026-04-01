package com.reservenook.companylifecycle.application

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companylifecycle.domain.CompanyDeletionEventStatus
import com.reservenook.companylifecycle.infrastructure.CompanyDeletionEventRepository
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.platformadmin.domain.InactivityPolicy
import com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository
import com.reservenook.registration.application.RegistrationMailSender
import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.CompanySubscription
import com.reservenook.registration.domain.PlanType
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant

@SpringBootTest
class CompanyDeletionServiceIntegrationTest(
    @Autowired private val companyDeletionService: CompanyDeletionService,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository,
    @Autowired private val passwordResetTokenRepository: PasswordResetTokenRepository,
    @Autowired private val inactivityNotificationEventRepository: InactivityNotificationEventRepository,
    @Autowired private val companyDeletionEventRepository: CompanyDeletionEventRepository,
    @Autowired private val inactivityPolicyRepository: InactivityPolicyRepository,
    @Autowired private val securityAuditEventRepository: SecurityAuditEventRepository
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
        securityAuditEventRepository.deleteAll()
        inactivityNotificationEventRepository.deleteAll()
        companyDeletionEventRepository.deleteAll()
        activationTokenRepository.deleteAll()
        passwordResetTokenRepository.deleteAll()
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
    fun `deletion job removes tenant owned data and records audit event`() {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-wellness",
                status = CompanyStatus.PENDING_DELETION,
                defaultLanguage = "en",
                defaultLocale = "en-US",
                lastActivityAt = Instant.parse("2025-12-01T00:00:00Z"),
                inactiveAt = Instant.parse("2026-01-12T12:00:00Z"),
                deletionScheduledAt = Instant.parse("2026-03-30T12:00:00Z")
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
        subscriptionRepository.save(
            CompanySubscription(
                company = company,
                planType = PlanType.TRIAL,
                startsAt = Instant.parse("2026-01-01T00:00:00Z"),
                expiresAt = Instant.parse("2026-04-01T00:00:00Z")
            )
        )
        activationTokenRepository.save(
            ActivationToken(
                token = "activation-token",
                company = company,
                user = user,
                expiresAt = Instant.parse("2026-04-01T00:00:00Z")
            )
        )
        passwordResetTokenRepository.save(
            PasswordResetToken(
                token = "reset-token",
                user = user,
                expiresAt = Instant.parse("2026-04-01T00:00:00Z")
            )
        )

        val result = companyDeletionService.deletePendingCompanies(Instant.parse("2026-03-30T12:00:00Z"))

        result.deletedCompanies shouldBe 1
        result.failedDeletions shouldBe 0
        companyRepository.findById(requireNotNull(company.id)).orElse(null).shouldBeNull()
        membershipRepository.findAllByCompanyId(requireNotNull(company.id)).shouldHaveSize(0)
        userAccountRepository.findById(requireNotNull(user.id)).orElse(null).shouldBeNull()
        companyDeletionEventRepository.findAllByCompanyId(requireNotNull(company.id)).shouldHaveSize(1)
        companyDeletionEventRepository.findAllByCompanyId(requireNotNull(company.id)).single().status shouldBe CompanyDeletionEventStatus.SUCCEEDED
        securityAuditEventRepository.findAll().any {
            it.eventType == SecurityAuditEventType.COMPANY_DELETED && it.companySlug == "acme-wellness"
        } shouldBe true
    }
}
