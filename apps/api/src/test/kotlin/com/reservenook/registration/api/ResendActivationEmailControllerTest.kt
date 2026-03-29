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
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
class ResendActivationEmailControllerTest(
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
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any()) }
        activationTokenRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `resend endpoint dispatches activation email for eligible account`() {
        justRun { registrationMailSender.sendActivationEmail(any(), any()) }
        seedPendingCompany(email = "admin@acme.com", tokenCreatedAt = Instant.now().minusSeconds(900))

        mockMvc.post("/api/public/companies/activation/resend") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ResendActivationEmailRequest("admin@acme.com"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("If the account is pending activation, a new activation email will be sent.") }
            }

        activationTokenRepository.findAll() shouldHaveSize 2
        verify(exactly = 1) { registrationMailSender.sendActivationEmail("admin@acme.com", any()) }
    }

    @Test
    fun `resend endpoint returns neutral response for unknown email`() {
        justRun { registrationMailSender.sendActivationEmail(any(), any()) }

        mockMvc.post("/api/public/companies/activation/resend") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ResendActivationEmailRequest("missing@acme.com"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("If the account is pending activation, a new activation email will be sent.") }
            }

        verify(exactly = 0) { registrationMailSender.sendActivationEmail(any(), any()) }
    }

    private fun seedPendingCompany(email: String, tokenCreatedAt: Instant) {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-${UUID.randomUUID()}",
                status = CompanyStatus.PENDING_ACTIVATION,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        val user = userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = "encoded",
                status = UserStatus.PENDING_ACTIVATION,
                emailVerified = false
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
                expiresAt = tokenCreatedAt.plusSeconds(3600),
                createdAt = tokenCreatedAt
            )
        )
    }
}
