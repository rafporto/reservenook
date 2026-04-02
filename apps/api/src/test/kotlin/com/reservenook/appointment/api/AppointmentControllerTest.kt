package com.reservenook.appointment.api

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.appointment.domain.AppointmentBooking
import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.domain.AppointmentProviderAvailability
import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderAvailabilityRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.booking.domain.CustomerContact
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
import com.reservenook.companybackoffice.domain.BusinessDay
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val appointmentServiceRepository: AppointmentServiceRepository,
    @Autowired private val appointmentProviderRepository: AppointmentProviderRepository,
    @Autowired private val appointmentProviderAvailabilityRepository: AppointmentProviderAvailabilityRepository,
    @Autowired private val appointmentBookingRepository: AppointmentBookingRepository,
    @Autowired private val customerContactRepository: CustomerContactRepository,
    @Autowired private val bookingRepository: BookingRepository,
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
        appointmentBookingRepository.deleteAll()
        appointmentProviderAvailabilityRepository.deleteAll()
        appointmentProviderRepository.deleteAll()
        appointmentServiceRepository.deleteAll()
        bookingRepository.deleteAll()
        customerContactRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `company admin can manage appointment services providers and availability`() {
        val admin = seedCompanyUser("acme-wellness", "admin@acme.com", "SecurePass123", CompanyRole.COMPANY_ADMIN)
        val staff = seedCompanyUser("acme-wellness", "provider@acme.com", "SecurePass123", CompanyRole.STAFF)
        val session = authenticatedCompanyAdminSession(requireNotNull(admin.id), "admin@acme.com", "acme-wellness")

        val serviceResult = mockMvc.post("/api/app/company/acme-wellness/appointment-services") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "name":"Initial consultation",
                  "description":"30 minute intro session",
                  "durationMinutes":30,
                  "bufferMinutes":10,
                  "priceLabel":"EUR 60",
                  "enabled":true,
                  "autoConfirm":false
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.appointmentService.name") { value("Initial consultation") }
        }.andReturn()

        val providerResult = mockMvc.post("/api/app/company/acme-wellness/appointment-providers") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "linkedUserId":${requireNotNull(staff.id)},
                  "displayName":"Anna Therapist",
                  "email":"provider@acme.com",
                  "active":true
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.appointmentProvider.displayName") { value("Anna Therapist") }
        }.andReturn()

        val providerId = providerResult.response.contentAsString.substringAfter("\"id\":").substringBefore(",").trim().toLong()

        mockMvc.put("/api/app/company/acme-wellness/appointment-providers/$providerId/availability") {
            with(csrf().asHeader())
            this.session = session
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "entries":[
                    {"dayOfWeek":"MONDAY","opensAt":"09:00","closesAt":"13:00","displayOrder":0}
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.providerSchedule.providerId") { value(providerId) }
            jsonPath("$.providerSchedule.availability[0].dayOfWeek") { value("MONDAY") }
        }

        mockMvc.get("/api/app/company/acme-wellness/appointment-services") {
            this.session = session
        }.andExpect {
            status { isOk() }
            jsonPath("$.appointmentServices.length()") { value(1) }
        }
    }

    @Test
    fun `linked provider can view only own schedule`() {
        seedCompanyUser("acme-wellness", "admin@acme.com", "SecurePass123", CompanyRole.COMPANY_ADMIN)
        val providerUser = seedCompanyUser("acme-wellness", "provider@acme.com", "SecurePass123", CompanyRole.STAFF)
        val company = companyRepository.findBySlug("acme-wellness") ?: error("Expected company")
        val appointmentService = appointmentServiceRepository.save(
            AppointmentService(
                company = company,
                name = "Initial consultation",
                durationMinutes = 30
            )
        )
        val provider = appointmentProviderRepository.save(
            AppointmentProvider(
                company = company,
                linkedUser = providerUser,
                displayName = "Anna Therapist",
                email = "provider@acme.com"
            )
        )
        appointmentProviderAvailabilityRepository.save(
            AppointmentProviderAvailability(
                provider = provider,
                dayOfWeek = BusinessDay.FRIDAY,
                opensAt = LocalTime.of(9, 0),
                closesAt = LocalTime.of(12, 0),
                displayOrder = 0
            )
        )
        val contact = customerContactRepository.save(
            CustomerContact(
                company = company,
                fullName = "Alex Guest",
                email = "alex@example.com",
                normalizedEmail = "alex@example.com"
            )
        )
        val booking = bookingRepository.save(
            Booking(
                company = company,
                customerContact = contact,
                status = BookingStatus.CONFIRMED,
                source = BookingSource.PUBLIC_WEB,
                requestSummary = "Initial consultation"
            )
        )
        appointmentBookingRepository.save(
            AppointmentBooking(
                booking = booking,
                company = company,
                appointmentService = appointmentService,
                provider = provider,
                startsAt = Instant.parse("2026-04-10T09:00:00Z"),
                endsAt = Instant.parse("2026-04-10T09:30:00Z")
            )
        )
        val session = authenticatedCompanyStaffSession(requireNotNull(providerUser.id), "provider@acme.com", "acme-wellness")

        mockMvc.get("/api/app/company/acme-wellness/providers/me/schedule") {
            this.session = session
            param("date", "2026-04-10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.entries.length()") { value(1) }
            jsonPath("$.entries[0].customerName") { value("Alex Guest") }
            jsonPath("$.entries[0].serviceName") { value("Initial consultation") }
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

    private fun seedSecurityTimestamps(session: MockHttpSession) {
        val nowMillis = System.currentTimeMillis()
        session.setAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, nowMillis)
        session.setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, nowMillis)
    }

    private fun seedCompanyUser(slug: String, email: String, password: String, role: CompanyRole): UserAccount {
        val company = companyRepository.findBySlug(slug)
            ?: companyRepository.save(
                Company(
                    name = "Acme Wellness",
                    businessType = BusinessType.APPOINTMENT,
                    slug = slug,
                    status = CompanyStatus.ACTIVE,
                    defaultLanguage = "en",
                    defaultLocale = "en-US",
                    widgetEnabled = true
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

        if (subscriptionRepository.findFirstByCompanyIdOrderByExpiresAtDesc(requireNotNull(company.id)) == null) {
            subscriptionRepository.save(
                CompanySubscription(
                    company = company,
                    planType = PlanType.TRIAL,
                    startsAt = Instant.now(),
                    expiresAt = Instant.now().plusSeconds(604800)
                )
            )
        }

        return user
    }
}
