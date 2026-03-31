package com.reservenook.platformadmin.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.AppAuthenticatedUser
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
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class PlatformAdminCompanyControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val inactivityPolicyRepository: com.reservenook.platformadmin.infrastructure.InactivityPolicyRepository,
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
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
        inactivityPolicyRepository.deleteAll()
        inactivityPolicyRepository.save(
            com.reservenook.platformadmin.domain.InactivityPolicy(
                id = 1L,
                inactivityThresholdDays = 90,
                deletionWarningLeadDays = 14
            )
        )
    }

    @Test
    fun `platform admin receives company list`() {
        seedCompany(
            name = "Acme Wellness",
            slug = "acme-wellness",
            businessType = BusinessType.APPOINTMENT,
            status = CompanyStatus.ACTIVE,
            planType = PlanType.TRIAL,
            expiresAt = Instant.parse("2026-04-05T00:00:00Z")
        )
        seedCompany(
            name = "Studio Norte",
            slug = "studio-norte",
            businessType = BusinessType.CLASS,
            status = CompanyStatus.PENDING_ACTIVATION,
            planType = PlanType.PAID,
            expiresAt = Instant.parse("2027-03-20T00:00:00Z")
        )
        seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(userId = 1L, email = "platform@reservenook.com")

        mockMvc.get("/api/platform-admin/companies") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.companies.length()") { value(2) }
                jsonPath("$.companies[0].companyName") { value("Studio Norte") }
                jsonPath("$.companies[0].planType") { value("PAID") }
                jsonPath("$.companies[1].companyName") { value("Acme Wellness") }
            }
    }

    @Test
    fun `company admin is forbidden from company list`() {
        seedCompany(
            name = "Acme Wellness",
            slug = "acme-wellness",
            businessType = BusinessType.APPOINTMENT,
            status = CompanyStatus.ACTIVE,
            planType = PlanType.TRIAL,
            expiresAt = Instant.parse("2026-04-05T00:00:00Z")
        )
        seedCompanyAdmin(email = "admin@acme.com", password = "SecurePass123", slug = "acme-wellness")

        val session = authenticatedCompanyAdminSession(userId = 2L, email = "admin@acme.com", companySlug = "acme-wellness")

        mockMvc.get("/api/platform-admin/companies") {
            this.session = session
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `platform admin receives current inactivity policy`() {
        seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(userId = 1L, email = "platform@reservenook.com")

        mockMvc.get("/api/platform-admin/inactivity-policy") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.inactivityThresholdDays") { value(90) }
                jsonPath("$.deletionWarningLeadDays") { value(14) }
            }
    }

    @Test
    fun `platform admin updates inactivity policy`() {
        seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(userId = 1L, email = "platform@reservenook.com")

        mockMvc.put("/api/platform-admin/inactivity-policy") {
            this.session = session
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateInactivityPolicyRequest(
                    inactivityThresholdDays = 120,
                    deletionWarningLeadDays = 21
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Inactivity policy updated.") }
                jsonPath("$.policy.inactivityThresholdDays") { value(120) }
                jsonPath("$.policy.deletionWarningLeadDays") { value(21) }
            }
    }

    @Test
    fun `invalid inactivity policy update is rejected`() {
        seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(userId = 1L, email = "platform@reservenook.com")

        mockMvc.put("/api/platform-admin/inactivity-policy") {
            this.session = session
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                UpdateInactivityPolicyRequest(
                    inactivityThresholdDays = 30,
                    deletionWarningLeadDays = 45
                )
            )
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("Deletion warning lead time cannot be greater than the inactivity threshold.") }
            }
    }

    private fun authenticatedPlatformAdminSession(userId: Long, email: String): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = true
        )
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN"))
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication

        return MockHttpSession().apply {
            setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
        }
    }

    private fun authenticatedCompanyAdminSession(userId: Long, email: String, companySlug: String): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = false,
            companySlug = companySlug
        )
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_COMPANY_ADMIN"))
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication

        return MockHttpSession().apply {
            setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
        }
    }

    private fun seedPlatformAdmin(email: String, password: String) {
        userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = passwordEncoder.encode(password),
                status = UserStatus.ACTIVE,
                emailVerified = true,
                isPlatformAdmin = true
            )
        )
    }

    private fun seedCompanyAdmin(email: String, password: String, slug: String) {
        val company = companyRepository.findAll().first { it.slug == slug }
        val user = userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = passwordEncoder.encode(password),
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
    }

    private fun seedCompany(
        name: String,
        slug: String,
        businessType: BusinessType,
        status: CompanyStatus,
        planType: PlanType,
        expiresAt: Instant
    ) {
        val company = companyRepository.save(
            Company(
                name = name,
                businessType = businessType,
                slug = slug,
                status = status,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

        subscriptionRepository.save(
            CompanySubscription(
                company = company,
                planType = planType,
                startsAt = Instant.parse("2026-03-20T00:00:00Z"),
                expiresAt = expiresAt
            )
        )
    }
}
