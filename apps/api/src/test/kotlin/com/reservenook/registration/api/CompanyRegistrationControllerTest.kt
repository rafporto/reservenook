package com.reservenook.registration.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mail.MailSendException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class CompanyRegistrationControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
        activationTokenRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `registers company and creates activation state`() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }

        val response = mockMvc.post("/api/public/companies/registration") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterCompanyRequest(
                    companyName = "Acme Wellness",
                    businessType = BusinessType.APPOINTMENT,
                    slug = "acme-wellness",
                    email = "admin@acme.com",
                    password = "SecurePass123",
                    planType = PlanType.TRIAL,
                    defaultLanguage = "en",
                    defaultLocale = "en-US"
                )
            )
        }
            .andExpect {
                status { isCreated() }
            }
            .andReturn()

        response.response.contentAsString.contains("Check your email") shouldBe true

        val companies = companyRepository.findAll()
        val users = userAccountRepository.findAll()
        val memberships = membershipRepository.findAll()
        val subscriptions = subscriptionRepository.findAll()
        val tokens = activationTokenRepository.findAll()

        companies shouldHaveSize 1
        users shouldHaveSize 1
        memberships shouldHaveSize 1
        subscriptions shouldHaveSize 1
        tokens shouldHaveSize 1

        val company = companies.single()
        val user = users.single()
        val membership = memberships.single()
        val subscription = subscriptions.single()
        val token = tokens.single()

        company.status shouldBe CompanyStatus.PENDING_ACTIVATION
        user.status shouldBe UserStatus.PENDING_ACTIVATION
        user.emailVerified shouldBe false
        membership.role shouldBe CompanyRole.COMPANY_ADMIN
        membership.company.id shouldBe company.id
        membership.user.id shouldBe user.id
        subscription.planType shouldBe PlanType.TRIAL
        token.company.id shouldBe company.id
        token.user.id shouldBe user.id

        verify(exactly = 1) { registrationMailSender.sendActivationEmail("admin@acme.com", any(), "en") }
    }

    @Test
    fun `rejects duplicate slug`() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        seedExistingCompany(slug = "duplicate-slug", email = "existing@company.com")

        mockMvc.post("/api/public/companies/registration") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterCompanyRequest(
                    companyName = "Duplicate Clinic",
                    businessType = BusinessType.APPOINTMENT,
                    slug = "duplicate-slug",
                    email = "new@company.com",
                    password = "SecurePass123",
                    planType = PlanType.TRIAL,
                    defaultLanguage = "en",
                    defaultLocale = "en-US"
                )
            )
        }
            .andExpect {
                status { isConflict() }
            }
    }

    @Test
    fun `rejects duplicate email`() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        seedExistingCompany(slug = "existing-slug", email = "existing@company.com")

        mockMvc.post("/api/public/companies/registration") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterCompanyRequest(
                    companyName = "Duplicate Email Clinic",
                    businessType = BusinessType.CLASS,
                    slug = "another-slug",
                    email = "existing@company.com",
                    password = "SecurePass123",
                    planType = PlanType.PAID,
                    defaultLanguage = "en",
                    defaultLocale = "en-US"
                )
            )
        }
            .andExpect {
                status { isConflict() }
            }
    }

    @Test
    fun `returns service unavailable when activation email cannot be sent`() {
        every { registrationMailSender.sendActivationEmail(any(), any(), any()) } throws MailSendException("SMTP unavailable")

        mockMvc.post("/api/public/companies/registration") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                RegisterCompanyRequest(
                    companyName = "Mail Failure Clinic",
                    businessType = BusinessType.APPOINTMENT,
                    slug = "mail-failure-clinic",
                    email = "admin@mailfailure.com",
                    password = "SecurePass123",
                    planType = PlanType.TRIAL,
                    defaultLanguage = "en",
                    defaultLocale = "en-US"
                )
            )
        }
            .andExpect {
                status { isServiceUnavailable() }
            }

        companyRepository.findAll() shouldHaveSize 0
    }

    private fun seedExistingCompany(slug: String, email: String) {
        val company = companyRepository.save(
            Company(
                name = "Existing Co",
                businessType = BusinessType.APPOINTMENT,
                slug = slug,
                status = CompanyStatus.PENDING_ACTIVATION,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        val user = userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = "hashed",
                status = UserStatus.PENDING_ACTIVATION
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
                startsAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(604800)
            )
        )

        activationTokenRepository.save(
            ActivationToken(
                token = UUID.randomUUID().toString(),
                company = company,
                user = user,
                expiresAt = Instant.now().plusSeconds(3600)
            )
        )
    }
}
