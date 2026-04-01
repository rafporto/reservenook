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
import com.reservenook.security.application.SessionSecurityAttributes
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.infrastructure.SecurityAuditEventRepository
import io.kotest.matchers.shouldBe
import io.mockk.justRun
import io.mockk.verify
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
    fun `company admin can update branding settings`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/branding") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "displayName": "Acme Studio",
                  "logoUrl": "https://cdn.acme.com/logo.svg",
                  "accentColor": "#B45A38",
                  "supportEmail": "support@acme.com",
                  "supportPhone": "+49 30 555 0101"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.branding.displayName") { value("Acme Studio") }
            jsonPath("$.branding.accentColor") { value("#B45A38") }
        }
    }

    @Test
    fun `company admin can update localization settings`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/localization") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"defaultLanguage":"de","defaultLocale":"de-DE"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.localization.defaultLanguage") { value("de") }
            jsonPath("$.company.defaultLocale") { value("de-DE") }
        }
    }

    @Test
    fun `company admin can update business hours and closure dates`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/business-hours") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"entries":[{"dayOfWeek":"MONDAY","opensAt":"09:00","closesAt":"17:00","displayOrder":0}]}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.businessHours[0].dayOfWeek") { value("MONDAY") }
        }

        mockMvc.put("/api/app/company/acme-wellness/closure-dates") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"entries":[{"label":"Holiday","startsOn":"2026-12-24","endsOn":"2026-12-26"}]}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.closureDates[0].label") { value("Holiday") }
        }
    }

    @Test
    fun `company admin can update notification preferences`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/notification-preferences") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "destinationEmail":"alerts@acme.com",
                  "notifyOnNewBooking":true,
                  "notifyOnCancellation":false,
                  "notifyDailySummary":true
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.notificationPreferences.destinationEmail") { value("alerts@acme.com") }
            jsonPath("$.notificationPreferences.notifyDailySummary") { value(true) }
        }
    }

    @Test
    fun `company admin can create and update a staff user`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        val createResult = mockMvc.post("/api/app/company/acme-wellness/staff") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"fullName":"Support Agent","email":"staff@acme.com","role":"STAFF"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.staffUser.email") { value("staff@acme.com") }
            jsonPath("$.staffUser.role") { value("STAFF") }
        }.andReturn()

        verify(exactly = 1) { passwordResetMailSender.sendPasswordResetEmail(eq("staff@acme.com"), any(), eq("en")) }

        val membershipId = objectMapper.readTree(createResult.response.contentAsString).path("staffUser").path("membershipId").asLong()

        mockMvc.put("/api/app/company/acme-wellness/staff/$membershipId") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"role":"COMPANY_ADMIN","status":"ACTIVE"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.staffUser.role") { value("COMPANY_ADMIN") }
        }
    }

    @Test
    fun `company admin can update customer questions and widget settings`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = loginCompanyAdminSession(companyAdminEmail, "SecurePass123")

        mockMvc.put("/api/app/company/acme-wellness/customer-questions") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "entries":[
                    {
                      "label":"Preferred provider",
                      "questionType":"SINGLE_SELECT",
                      "required":true,
                      "enabled":true,
                      "displayOrder":0,
                      "options":["Any","Anna","Luis"]
                    }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.customerQuestions[0].label") { value("Preferred provider") }
        }

        mockMvc.put("/api/app/company/acme-wellness/widget-settings") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "ctaLabel":"Reserve now",
                  "widgetEnabled":true,
                  "allowedDomains":["booking.acme.com"],
                  "themeVariant":"soft"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.widgetSettings.widgetEnabled") { value(true) }
            jsonPath("$.widgetSettings.themeVariant") { value("soft") }
        }
    }

    @Test
    fun `company staff cannot access admin backoffice routes`() {
        val staff = seedCompanyUser(
            slug = "acme-wellness",
            email = "staff@acme.com",
            password = "SecurePass123",
            role = CompanyRole.STAFF
        )
        val session = authenticatedCompanyStaffSession(requireNotNull(staff.id), "staff@acme.com", "acme-wellness")

        mockMvc.get("/api/app/company/acme-wellness/backoffice") {
            this.session = session
        }.andExpect {
            status { isForbidden() }
        }

        mockMvc.get("/api/app/company/acme-wellness/staff") {
            this.session = session
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `platform admin cannot access tenant company configuration routes`() {
        seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val platformAdmin = seedPlatformAdmin(email = "platform@reservenook.com", password = "SecurePass123")
        val session = authenticatedPlatformAdminSession(userId = requireNotNull(platformAdmin.id), email = "platform@reservenook.com")

        mockMvc.get("/api/app/company/acme-wellness/backoffice") {
            this.session = session
        }.andExpect {
            status { isForbidden() }
        }

        mockMvc.put("/api/app/company/acme-wellness/widget-settings") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "ctaLabel":"Reserve now",
                  "widgetEnabled":true,
                  "allowedDomains":["booking.acme.com"],
                  "themeVariant":"soft"
                }
            """.trimIndent()
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `widget settings update requires csrf token`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/widget-settings") {
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "ctaLabel":"Reserve now",
                  "widgetEnabled":true,
                  "allowedDomains":["booking.acme.com"],
                  "themeVariant":"soft"
                }
            """.trimIndent()
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `company branding update requires recent authentication`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")
        session.setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, System.currentTimeMillis() - 901_000)

        mockMvc.put("/api/app/company/acme-wellness/branding") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "displayName": "Acme Studio",
                  "logoUrl": "https://cdn.acme.com/logo.svg",
                  "accentColor": "#B45A38",
                  "supportEmail": "support@acme.com",
                  "supportPhone": "+49 30 555 0101"
                }
            """.trimIndent()
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.message") { value("Please sign in again before performing this sensitive action.") }
        }
    }

    @Test
    fun `company admin cannot update a staff membership from another tenant by id guessing`() {
        val admin = seedCompanyAdmin(slug = "acme-wellness", email = companyAdminEmail, password = "SecurePass123")
        val otherUser = seedCompanyUser(
            slug = "other-company",
            email = "other-staff@acme.com",
            password = "SecurePass123",
            role = CompanyRole.STAFF
        )
        val otherMembership = membershipRepository.findFirstByUserIdAndCompanySlug(requireNotNull(otherUser.id), "other-company")
            ?: error("Expected other-company membership")
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), companyAdminEmail, "acme-wellness")

        mockMvc.put("/api/app/company/acme-wellness/staff/${requireNotNull(otherMembership.id)}") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"role":"STAFF","status":"ACTIVE"}"""
        }.andExpect {
            status { isBadRequest() }
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
            companySlug = companySlug,
            companyRole = CompanyRole.COMPANY_ADMIN.name
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

    private fun authenticatedCompanyStaffSession(userId: Long, email: String, companySlug: String): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = false,
            companySlug = companySlug,
            companyRole = CompanyRole.STAFF.name
        )
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority("ROLE_COMPANY_STAFF"))
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication

        return MockHttpSession().apply {
            setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            seedSecurityTimestamps(this)
        }
    }

    private fun authenticatedPlatformAdminSession(userId: Long, email: String): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = true,
            companySlug = null,
            companyRole = null
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

    private fun seedSecurityTimestamps(session: MockHttpSession) {
        val nowMillis = System.currentTimeMillis()
        session.setAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, nowMillis)
    }

    private fun loginCompanyAdminSession(email: String, password: String): MockHttpSession {
        val uniqueClientAddress = "10.0.17.${(System.nanoTime() % 200).toInt() + 20}"
        val loginResult = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/public/auth/login")
                .with { request ->
                    request.remoteAddr = uniqueClientAddress
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
        return seedCompanyUser(slug, email, password, CompanyRole.COMPANY_ADMIN)
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

    private fun seedCompanyUser(slug: String, email: String, password: String, role: CompanyRole): UserAccount {
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
                role = role
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
