package com.reservenook.widget.api

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
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
import com.reservenook.widget.infrastructure.WidgetUsageEventRepository
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class WidgetControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val widgetUsageEventRepository: WidgetUsageEventRepository,
    @Autowired private val customerContactRepository: CustomerContactRepository,
    @Autowired private val bookingRepository: BookingRepository,
    @Autowired private val bookingAuditEventRepository: BookingAuditEventRepository,
    @Autowired private val requestThrottleService: RequestThrottleService,
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
        SecurityContextHolder.clearContext()
        requestThrottleService.clearAll()
        widgetUsageEventRepository.deleteAll()
        bookingAuditEventRepository.deleteAll()
        bookingRepository.deleteAll()
        customerContactRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `allowed origin can bootstrap widget and widget token counts booking usage`() {
        seedCompany("acme-widget")
        val admin = seedAdmin("acme-widget")

        val bootstrapResponse = mockMvc.get("/api/public/widget/acme-widget/bootstrap") {
            header("Origin", "https://booking.acme.com")
            param("locale", "en")
        }.andExpect {
            status { isOk() }
            header { string("Access-Control-Allow-Origin", "https://booking.acme.com") }
            jsonPath("$.companySlug") { value("acme-widget") }
        }.andReturn()

        val token = bootstrapResponse.response.contentAsString.substringAfter("\"widgetToken\":\"").substringBefore("\"")

        mockMvc.post("/api/public/companies/acme-widget/booking-intake") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            header("X-ReserveNook-Widget-Token", token)
            content = """
                {
                  "fullName":"Alex Guest",
                  "email":"alex@example.com",
                  "phone":null,
                  "preferredLanguage":"en",
                  "requestSummary":"Widget booking",
                  "preferredDate":"2026-04-22",
                  "notes":null
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
        }

        val session = authenticatedAdminSession(requireNotNull(admin.id), "admin@acme.com", "acme-widget")
        mockMvc.get("/api/app/company/acme-widget/widget-usage") {
            this.session = session
        }.andExpect {
            status { isOk() }
            jsonPath("$.bootstrapsLast7Days") { value(1) }
            jsonPath("$.bookingsLast7Days") { value(1) }
            jsonPath("$.recentOrigins[0].originHost") { value("booking.acme.com") }
        }
    }

    @Test
    fun `disallowed origin cannot bootstrap widget`() {
        seedCompany("acme-widget")

        mockMvc.get("/api/public/widget/acme-widget/bootstrap") {
            header("Origin", "https://evil.example")
            param("locale", "en")
        }.andExpect {
            status { isForbidden() }
        }
    }

    private fun seedCompany(slug: String): Company =
        companyRepository.save(
            Company(
                name = "Acme Widget",
                businessType = BusinessType.APPOINTMENT,
                slug = slug,
                status = CompanyStatus.ACTIVE,
                defaultLanguage = "en",
                defaultLocale = "en-US",
                widgetEnabled = true,
                widgetAllowedDomains = "booking.acme.com",
                widgetThemeVariant = "soft"
            )
        ).also { company ->
            subscriptionRepository.save(
                CompanySubscription(
                    company = company,
                    planType = PlanType.TRIAL,
                    startsAt = Instant.now(),
                    expiresAt = Instant.now().plusSeconds(604800)
                )
            )
        }

    private fun seedAdmin(slug: String): UserAccount {
        val company = companyRepository.findBySlug(slug) ?: error("Expected company")
        val user = userAccountRepository.save(
            UserAccount(
                email = "admin@acme.com",
                passwordHash = passwordEncoder.encode("SecurePass123"),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )
        membershipRepository.save(CompanyMembership(company = company, user = user, role = CompanyRole.COMPANY_ADMIN))
        return user
    }

    private fun authenticatedAdminSession(userId: Long, email: String, companySlug: String): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = false,
            companySlug = companySlug,
            companyRole = CompanyRole.COMPANY_ADMIN.name
        )
        val authentication = UsernamePasswordAuthenticationToken(principal, null, listOf(SimpleGrantedAuthority("ROLE_COMPANY_ADMIN")))
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        return MockHttpSession().apply {
            setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            val now = System.currentTimeMillis()
            setAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS, now)
            setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, now)
            setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, now)
        }
    }
}
