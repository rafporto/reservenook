package com.reservenook.auth.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
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
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val activationTokenRepository: ActivationTokenRepository,
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { registrationMailSender.sendActivationEmail(any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any()) }
        activationTokenRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `login endpoint authenticates company admin and creates session`() {
        seedCompanyAdmin(email = "admin@acme.com", password = "SecurePass123")

        val result = mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "SecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.redirectTo") { value("/app/company/acme-wellness") }
            }
            .andReturn()

        (result.request.session!!.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) != null) shouldBe true
    }

    @Test
    fun `logout invalidates current session and protected session endpoint fails afterwards`() {
        seedCompanyAdmin(email = "admin@acme.com", password = "SecurePass123")

        val loginResult = mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "SecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val session = loginResult.request.session as MockHttpSession

        mockMvc.get("/api/auth/session") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.email") { value("admin@acme.com") }
            }

        mockMvc.post("/api/auth/logout") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.redirectTo") { value("/en/login") }
            }

        mockMvc.get("/api/auth/session") {
            this.session = session
        }
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `login endpoint rejects unverified user with activation guidance`() {
        seedCompanyAdmin(
            email = "admin@acme.com",
            password = "SecurePass123",
            userStatus = UserStatus.PENDING_ACTIVATION,
            emailVerified = false,
            companyStatus = CompanyStatus.PENDING_ACTIVATION
        )

        mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "admin@acme.com",
                    password = "SecurePass123"
                )
            )
        }
            .andExpect {
                status { isForbidden() }
                jsonPath("$.code") { value("ACTIVATION_REQUIRED") }
            }
    }

    @Test
    fun `login endpoint authenticates platform admin`() {
        userAccountRepository.save(
            UserAccount(
                email = "platform@reservenook.com",
                passwordHash = passwordEncoder.encode("SecurePass123"),
                status = UserStatus.ACTIVE,
                emailVerified = true,
                isPlatformAdmin = true
            )
        )

        mockMvc.post("/api/public/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "platform@reservenook.com",
                    password = "SecurePass123"
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.redirectTo") { value("/platform-admin") }
            }
    }

    private fun seedCompanyAdmin(
        email: String,
        password: String,
        userStatus: UserStatus = UserStatus.ACTIVE,
        emailVerified: Boolean = true,
        companyStatus: CompanyStatus = CompanyStatus.ACTIVE
    ) {
        val company = companyRepository.save(
            Company(
                name = "Acme Wellness",
                businessType = BusinessType.APPOINTMENT,
                slug = "acme-wellness",
                status = companyStatus,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        val user = userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = passwordEncoder.encode(password),
                status = userStatus,
                emailVerified = emailVerified
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
    }
}
