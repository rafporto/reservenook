package com.reservenook.registration.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.registration.application.RegistrationMailSender
import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.PlanType
import com.reservenook.registration.domain.CompanySubscription
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.kotest.matchers.shouldBe
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
class CompanyActivationControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val companySubscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        io.mockk.justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any()) }
        activationTokenRepository.deleteAll()
        companySubscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `activates company and user for valid token`() {
        val token = seedActivationToken("valid-token", expiresAt = Instant.now().plusSeconds(3600))

        mockMvc.post("/api/public/companies/activation/confirm") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ActivateCompanyRequest(token.token))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("ACTIVATED") }
            }

        val company = companyRepository.findById(token.company.id!!).orElseThrow()
        val user = userAccountRepository.findById(token.user.id!!).orElseThrow()
        val updatedToken = activationTokenRepository.findById(token.id!!).orElseThrow()

        company.status shouldBe CompanyStatus.ACTIVE
        user.status shouldBe UserStatus.ACTIVE
        user.emailVerified shouldBe true
        (updatedToken.usedAt != null) shouldBe true
    }

    @Test
    fun `returns expired outcome for expired token`() {
        seedActivationToken("expired-token", expiresAt = Instant.now().minusSeconds(60))

        mockMvc.post("/api/public/companies/activation/confirm") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ActivateCompanyRequest("expired-token"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("EXPIRED") }
            }
    }

    @Test
    fun `returns already active outcome for reused token`() {
        seedActivationToken(
            tokenValue = "used-token",
            expiresAt = Instant.now().plusSeconds(3600),
            usedAt = Instant.now().minusSeconds(30),
            companyStatus = CompanyStatus.ACTIVE,
            userStatus = UserStatus.ACTIVE,
            emailVerified = true
        )

        mockMvc.post("/api/public/companies/activation/confirm") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ActivateCompanyRequest("used-token"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("ALREADY_ACTIVE") }
            }
    }

    @Test
    fun `returns invalid outcome for unknown token`() {
        mockMvc.post("/api/public/companies/activation/confirm") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(ActivateCompanyRequest("missing-token"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("INVALID") }
            }
    }

    private fun seedActivationToken(
        tokenValue: String,
        expiresAt: Instant,
        usedAt: Instant? = null,
        companyStatus: CompanyStatus = CompanyStatus.PENDING_ACTIVATION,
        userStatus: UserStatus = UserStatus.PENDING_ACTIVATION,
        emailVerified: Boolean = false
    ): ActivationToken {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-${UUID.randomUUID()}",
                status = companyStatus,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        val user = userAccountRepository.save(
            UserAccount(
                email = "admin-${UUID.randomUUID()}@acme.com",
                passwordHash = "hashed",
                status = userStatus,
                emailVerified = emailVerified
            )
        )

        companySubscriptionRepository.save(
            CompanySubscription(
                company = company,
                planType = PlanType.TRIAL,
                startsAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(604800)
            )
        )

        return activationTokenRepository.save(
            ActivationToken(
                token = tokenValue,
                company = company,
                user = user,
                expiresAt = expiresAt,
                usedAt = usedAt
            )
        )
    }
}
