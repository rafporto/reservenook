package com.reservenook.auth.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.registration.application.RegistrationMailSender
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
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@AutoConfigureMockMvc
class ResetPasswordControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository,
    @Autowired private val passwordResetTokenRepository: PasswordResetTokenRepository,
    @Autowired private val passwordEncoder: PasswordEncoder
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
    fun `reset password endpoint updates stored password and invalidates token`() {
        val user = userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = passwordEncoder.encode("OldSecurePass123"),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )
        val token = passwordResetTokenRepository.save(
            PasswordResetToken(
                token = "valid-token",
                user = user,
                expiresAt = Instant.now().plus(2, ChronoUnit.HOURS)
            )
        )

        mockMvc.post("/api/public/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ResetPasswordRequest(
                    token = token.token,
                    password = "NewSecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Your password has been updated. Continue to login.") }
                jsonPath("$.redirectTo") { value("/en/login") }
            }

        val updatedUser = userAccountRepository.findByEmail("admin@acme.com")!!
        passwordEncoder.matches("NewSecurePass123", updatedUser.passwordHash) shouldBe true
        passwordResetTokenRepository.findByToken("valid-token")!!.usedAt.shouldNotBeNull()
    }

    @Test
    fun `reset password revokes authenticated sessions created before the password change`() {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-wellness",
                status = CompanyStatus.ACTIVE,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )
        val user = userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = passwordEncoder.encode("OldSecurePass123"),
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
                startsAt = Instant.now(),
                expiresAt = Instant.now().plus(2, ChronoUnit.DAYS)
            )
        )
        val token = passwordResetTokenRepository.save(
            PasswordResetToken(
                token = "valid-token",
                user = user,
                expiresAt = Instant.now().plus(2, ChronoUnit.HOURS)
            )
        )

        val loginResult = mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "OldSecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val session = loginResult.request.session as MockHttpSession

        mockMvc.post("/api/public/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ResetPasswordRequest(
                    token = token.token,
                    password = "NewSecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
            }

        mockMvc.get("/api/auth/session") {
            this.session = session
        }
            .andExpect {
                status { isUnauthorized() }
            }

        mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "OldSecurePass123"
                )
            )
        }
            .andExpect {
                status { isUnauthorized() }
            }

        mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "NewSecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.redirectTo") { value("/app/company/acme-wellness") }
            }
    }

    @Test
    fun `reset password endpoint rejects expired token`() {
        val user = userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = passwordEncoder.encode("OldSecurePass123"),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )
        passwordResetTokenRepository.save(
            PasswordResetToken(
                token = "expired-token",
                user = user,
                expiresAt = Instant.now().minus(1, ChronoUnit.MINUTES)
            )
        )

        mockMvc.post("/api/public/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ResetPasswordRequest(
                    token = "expired-token",
                    password = "NewSecurePass123"
                )
            )
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("EXPIRED_TOKEN") }
            }
    }

    @Test
    fun `reset password endpoint validates password policy`() {
        mockMvc.post("/api/public/auth/reset-password") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                ResetPasswordRequest(
                    token = "missing-token",
                    password = "short"
                )
            )
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("size must be between 8 and 255") }
            }
    }
}
