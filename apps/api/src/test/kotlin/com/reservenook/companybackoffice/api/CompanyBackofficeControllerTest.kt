package com.reservenook.companybackoffice.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.api.LoginRequest
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
import com.reservenook.security.application.RequestThrottleService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class CompanyBackofficeControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val requestThrottleService: RequestThrottleService,
    @Autowired private val securityAuditEventRepository: SecurityAuditEventRepository,
    @Autowired private val passwordEncoder: PasswordEncoder
) {

    private val companyAdminEmail = "backoffice-admin@acme.com"

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun cleanDatabase() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
        requestThrottleService.clearAll()
        securityAuditEventRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `company admin can access own tenant backoffice`() {
        val admin = seedCompanyAdmin(
            slug = "acme-wellness",
            email = companyAdminEmail,
            password = "SecurePass123"
        )
        val company = companyRepository.findAll().first()
        company.contactEmail = "hello@acme.com"
        companyRepository.save(company)

        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.get("/api/app/company/acme-wellness/backoffice") {
            this.session = session
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.company.companySlug") { value("acme-wellness") }
                jsonPath("$.company.companyName") { value("Acme Wellness") }
                jsonPath("$.profile.contactEmail") { value("hello@acme.com") }
                jsonPath("$.viewer.role") { value("COMPANY_ADMIN") }
                jsonPath("$.viewer.currentUserEmail") { value(companyAdminEmail) }
                jsonPath("$.operations.planType") { value("TRIAL") }
                jsonPath("$.configurationAreas[0].key") { value("profile") }
                jsonPath("$.configurationAreas[0].status") { value("available") }
            }
    }

    @Test
    fun `company admin can update own tenant profile`() {
        val admin = seedCompanyAdmin(
            slug = "acme-wellness",
            email = companyAdminEmail,
            password = "SecurePass123"
        )

        val company = companyRepository.findAll().first()
        company.contactEmail = "old@acme.com"
        company.contactPhone = "+49 30 111 1111"
        company.addressLine1 = "Old Street 1"
        company.city = "Berlin"
        company.postalCode = "10000"
        company.countryCode = "DE"
        companyRepository.save(company)

        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.put("/api/app/company/acme-wellness/profile") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "companyName": "Acme Wellness Studio",
                  "businessDescription": "Premium appointments and classes.",
                  "contactEmail": "hello@acme.com",
                  "contactPhone": "+49 30 555 0000",
                  "addressLine1": "Alexanderplatz 1",
                  "addressLine2": "Floor 3",
                  "city": "Berlin",
                  "postalCode": "10178",
                  "countryCode": "DE"
                }
            """.trimIndent()
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.message") { value("Company profile updated.") }
                jsonPath("$.company.companyName") { value("Acme Wellness Studio") }
                jsonPath("$.profile.contactEmail") { value("hello@acme.com") }
                jsonPath("$.profile.countryCode") { value("DE") }
            }

        securityAuditEventRepository.findAll().any { it.eventType == SecurityAuditEventType.COMPANY_PROFILE_UPDATED } shouldBe true
    }

    @Test
    fun `company admin cannot update another tenant profile`() {
        val admin = seedCompanyAdmin(
            slug = "acme-wellness",
            email = companyAdminEmail,
            password = "SecurePass123"
        )
        seedCompanyAdmin(
            slug = "other-company",
            email = "other@acme.com",
            password = "SecurePass123"
        )

        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.put("/api/app/company/other-company/profile") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "companyName": "Other Company",
                  "businessDescription": "Description",
                  "contactEmail": "hello@other.com",
                  "contactPhone": "+49 30 555 0000",
                  "addressLine1": "Street 1",
                  "addressLine2": null,
                  "city": "Berlin",
                  "postalCode": "10178",
                  "countryCode": "DE"
                }
            """.trimIndent()
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `company profile update requires csrf token`() {
        val admin = seedCompanyAdmin(
            slug = "acme-wellness",
            email = companyAdminEmail,
            password = "SecurePass123"
        )

        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.put("/api/app/company/acme-wellness/profile") {
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "companyName": "Acme Wellness Studio",
                  "businessDescription": "Premium appointments and classes.",
                  "contactEmail": "hello@acme.com",
                  "contactPhone": "+49 30 555 0000",
                  "addressLine1": "Alexanderplatz 1",
                  "addressLine2": "Floor 3",
                  "city": "Berlin",
                  "postalCode": "10178",
                  "countryCode": "DE"
                }
            """.trimIndent()
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `company admin cannot access another tenant backoffice`() {
        val admin = seedCompanyAdmin(
            slug = "acme-wellness",
            email = companyAdminEmail,
            password = "SecurePass123"
        )
        seedCompanyAdmin(
            slug = "other-company",
            email = "other@acme.com",
            password = "SecurePass123"
        )

        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.get("/api/app/company/other-company/backoffice") {
            this.session = session
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `company backoffice endpoint requires authentication`() {
        mockMvc.get("/api/app/company/acme-wellness/backoffice")
            .andExpect {
                status { isUnauthorized() }
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

    private fun loginCompanyAdminSession(email: String, password: String): MockHttpSession {
        val loginResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/public/auth/login")
                .with { request ->
                    request.remoteAddr = "10.0.17.18"
                    request
                }
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        LoginRequest(
                            email = email,
                            password = password
                        )
                    )
                )
        )
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk)
            .andReturn()

        return loginResult.request.session as MockHttpSession
    }

    private fun seedCompanyAdmin(slug: String, email: String, password: String): UserAccount {
        val company = companyRepository.save(
            Company(
                name = slug.split("-").joinToString(" ") { part -> part.replaceFirstChar(Char::titlecase) },
                businessType = BusinessType.APPOINTMENT,
                slug = slug,
                status = CompanyStatus.ACTIVE,
                defaultLanguage = "en",
                defaultLocale = "en-US"
            )
        )

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

        subscriptionRepository.save(
            CompanySubscription(
                company = company,
                planType = PlanType.TRIAL,
                startsAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(604800)
            )
        )

        return user
    }
}
