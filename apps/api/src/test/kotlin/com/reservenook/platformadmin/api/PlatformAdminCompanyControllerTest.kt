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
import com.reservenook.security.application.SessionSecurityAttributes
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import io.kotest.matchers.shouldBe
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
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
    @Autowired private val abusePreventionPolicyRepository: com.reservenook.platformadmin.infrastructure.AbusePreventionPolicyRepository,
    @Autowired private val securityAuditEventRepository: SecurityAuditEventRepository,
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
        securityAuditEventRepository.deleteAll()
        inactivityPolicyRepository.deleteAll()
        abusePreventionPolicyRepository.deleteAll()
        inactivityPolicyRepository.save(
            com.reservenook.platformadmin.domain.InactivityPolicy(
                id = 1L,
                inactivityThresholdDays = 90,
                deletionWarningLeadDays = 14
            )
        )
        abusePreventionPolicyRepository.save(
            com.reservenook.platformadmin.domain.AbusePreventionPolicy(
                id = 1L,
                loginPairLimit = 5,
                loginClientLimit = 10,
                loginEmailLimit = 10,
                publicWritePairLimit = 5,
                publicWriteClientLimit = 10,
                publicWriteEmailLimit = 10,
                publicReadClientLimit = 20
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
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        )

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
        val companyAdmin = seedCompanyAdmin(email = "admin@acme.com", password = "SecurePass123", slug = "acme-wellness")

        val session = authenticatedCompanyAdminSession(
            userId = requireNotNull(companyAdmin.id),
            email = "admin@acme.com",
            companySlug = "acme-wellness"
        )

        mockMvc.get("/api/platform-admin/companies") {
            this.session = session
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `platform admin receives current inactivity policy`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        )

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
    fun `platform admin receives current abuse policy`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")
        val session = authenticatedPlatformAdminSession(requireNotNull(platformAdmin.id), "platform@reservenook.com")

        mockMvc.get("/api/platform-admin/abuse-policy") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.loginPairLimit") { value(5) }
                jsonPath("$.publicReadClientLimit") { value(20) }
            }
    }

    @Test
    fun `platform admin updates inactivity policy`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        )

        mockMvc.put("/api/platform-admin/inactivity-policy") {
            with(csrf().asHeader())
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

        securityAuditEventRepository.findAll().any { it.eventType == SecurityAuditEventType.PLATFORM_POLICY_UPDATED } shouldBe true
    }

    @Test
    fun `invalid inactivity policy update is rejected`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        )

        mockMvc.put("/api/platform-admin/inactivity-policy") {
            with(csrf().asHeader())
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

    @Test
    fun `platform admin update requires csrf token`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        )

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
                status { isForbidden() }
            }
    }

    @Test
    fun `platform admin update requires recent authentication`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")

        val session = authenticatedPlatformAdminSession(
            userId = requireNotNull(platformAdmin.id),
            email = "platform@reservenook.com"
        ).apply {
            setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, System.currentTimeMillis() - 901_000)
        }

        mockMvc.put("/api/platform-admin/inactivity-policy") {
            with(csrf().asHeader())
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
                status { isUnauthorized() }
                jsonPath("$.message") { value("Please sign in again before performing this sensitive action.") }
            }
    }

    @Test
    fun `platform admin updates abuse policy`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")
        val session = authenticatedPlatformAdminSession(requireNotNull(platformAdmin.id), "platform@reservenook.com")

        mockMvc.put("/api/platform-admin/abuse-policy") {
            with(csrf().asHeader())
            this.session = session
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf(
                    "loginPairLimit" to 6,
                    "loginClientLimit" to 12,
                    "loginEmailLimit" to 12,
                    "publicWritePairLimit" to 6,
                    "publicWriteClientLimit" to 12,
                    "publicWriteEmailLimit" to 12,
                    "publicReadClientLimit" to 24
                )
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.policy.loginPairLimit") { value(6) }
                jsonPath("$.policy.publicReadClientLimit") { value(24) }
            }

        securityAuditEventRepository.findAll().any { it.eventType == SecurityAuditEventType.ABUSE_POLICY_UPDATED } shouldBe true
    }

    @Test
    fun `platform admin updates company legal hold`() {
        seedCompany(
            name = "Acme Wellness",
            slug = "acme-wellness",
            businessType = BusinessType.APPOINTMENT,
            status = CompanyStatus.ACTIVE,
            planType = PlanType.TRIAL,
            expiresAt = Instant.parse("2026-04-05T00:00:00Z")
        )
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")
        val session = authenticatedPlatformAdminSession(requireNotNull(platformAdmin.id), "platform@reservenook.com")

        mockMvc.put("/api/platform-admin/companies/acme-wellness/retention") {
            with(csrf().asHeader())
            this.session = session
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                mapOf("legalHoldUntil" to "2026-06-01T10:00:00Z")
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.legalHoldUntil") { value("2026-06-01T10:00:00Z") }
            }

        companyRepository.findBySlug("acme-wellness")?.legalHoldUntil shouldBe Instant.parse("2026-06-01T10:00:00Z")
    }

    @Test
    fun `platform admin receives operations summary`() {
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")
        securityAuditEventRepository.save(
            com.reservenook.security.domain.SecurityAuditEvent(
                eventType = SecurityAuditEventType.LOGIN_FAILURE,
                outcome = com.reservenook.security.domain.SecurityAuditOutcome.FAILURE,
                actorEmail = "operator@reservenook.com"
            )
        )
        val session = authenticatedPlatformAdminSession(requireNotNull(platformAdmin.id), "platform@reservenook.com")

        mockMvc.get("/api/platform-admin/operations-summary") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.summary.auditEventsLast24Hours") { value(1) }
                jsonPath("$.summary.loginFailuresLast24Hours") { value(1) }
                jsonPath("$.summary.alertingEnabled") { value(false) }
                jsonPath("$.securityAudit.length()") { value(1) }
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
            seedSecurityTimestamps(this)
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
            seedSecurityTimestamps(this)
        }
    }

    private fun seedSecurityTimestamps(session: MockHttpSession) {
        val nowMillis = System.currentTimeMillis()
        session.setAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, nowMillis)
    }

    private fun seedPlatformAdmin(email: String, password: String): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                email = email,
                passwordHash = passwordEncoder.encode(password),
                status = UserStatus.ACTIVE,
                emailVerified = true,
                isPlatformAdmin = true
            )
        )
    }

    private fun seedCompanyAdmin(email: String, password: String, slug: String): UserAccount {
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

        return user
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
