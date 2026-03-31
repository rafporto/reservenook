package com.reservenook.auth.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.registration.application.RegistrationMailSender
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

@SpringBootTest
@AutoConfigureMockMvc
class RequestPasswordResetControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository,
    @Autowired private val passwordResetTokenRepository: PasswordResetTokenRepository
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
        activationTokenRepository.deleteAll()
        passwordResetTokenRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `forgot password endpoint dispatches reset email for eligible account`() {
        userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = "encoded",
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )

        mockMvc.post("/api/public/auth/forgot-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RequestPasswordResetRequest("admin@acme.com"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("If the account is eligible, a password reset email will be sent.") }
            }

        passwordResetTokenRepository.findAll() shouldHaveSize 1
        verify(exactly = 1) { passwordResetMailSender.sendPasswordResetEmail("admin@acme.com", any(), "en") }
    }

    @Test
    fun `forgot password endpoint returns neutral response for unknown email`() {
        mockMvc.post("/api/public/auth/forgot-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RequestPasswordResetRequest("missing@acme.com"))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("If the account is eligible, a password reset email will be sent.") }
            }

        verify(exactly = 0) { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
    }
}
