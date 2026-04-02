package com.reservenook.restaurant.api

import com.ninjasquad.springmockk.MockkBean
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
import com.reservenook.restaurant.domain.DiningArea
import com.reservenook.restaurant.domain.RestaurantReservation
import com.reservenook.restaurant.domain.RestaurantReservationStatus
import com.reservenook.restaurant.domain.RestaurantReservationTable
import com.reservenook.restaurant.domain.RestaurantServicePeriod
import com.reservenook.restaurant.domain.RestaurantTable
import com.reservenook.restaurant.infrastructure.DiningAreaRepository
import com.reservenook.restaurant.infrastructure.RestaurantReservationRepository
import com.reservenook.restaurant.infrastructure.RestaurantReservationTableRepository
import com.reservenook.restaurant.infrastructure.RestaurantServicePeriodRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableCombinationRepository
import com.reservenook.restaurant.infrastructure.RestaurantTableRepository
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
class RestaurantControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val userAccountRepository: UserAccountRepository,
    @Autowired private val membershipRepository: CompanyMembershipRepository,
    @Autowired private val subscriptionRepository: CompanySubscriptionRepository,
    @Autowired private val diningAreaRepository: DiningAreaRepository,
    @Autowired private val restaurantTableRepository: RestaurantTableRepository,
    @Autowired private val restaurantTableCombinationRepository: RestaurantTableCombinationRepository,
    @Autowired private val restaurantServicePeriodRepository: RestaurantServicePeriodRepository,
    @Autowired private val restaurantReservationRepository: RestaurantReservationRepository,
    @Autowired private val restaurantReservationTableRepository: RestaurantReservationTableRepository,
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
        restaurantReservationTableRepository.deleteAll()
        restaurantReservationRepository.deleteAll()
        restaurantTableCombinationRepository.deleteAll()
        restaurantTableRepository.deleteAll()
        diningAreaRepository.deleteAll()
        restaurantServicePeriodRepository.deleteAll()
        bookingRepository.deleteAll()
        customerContactRepository.deleteAll()
        membershipRepository.deleteAll()
        subscriptionRepository.deleteAll()
        userAccountRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `company admin can configure restaurant floor and staff can operate floorbook`() {
        val admin = seedCompanyUser("acme-bistro", "admin@acme.com", CompanyRole.COMPANY_ADMIN)
        val staff = seedCompanyUser("acme-bistro", "staff@acme.com", CompanyRole.STAFF)
        val adminSession = authenticatedSession(requireNotNull(admin.id), "admin@acme.com", "acme-bistro", CompanyRole.COMPANY_ADMIN)

        val areaResponse = mockMvc.post("/api/app/company/acme-bistro/dining-areas") {
            with(csrf().asHeader())
            this.session = adminSession
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"name":"Main Hall","displayOrder":0,"active":true}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.diningArea.name") { value("Main Hall") }
        }.andReturn()
        val areaId = areaResponse.response.contentAsString.substringAfter("\"id\":").substringBefore(",").trim().toLong()

        val tableResponse = mockMvc.post("/api/app/company/acme-bistro/restaurant-tables") {
            with(csrf().asHeader())
            this.session = adminSession
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"diningAreaId":$areaId,"label":"T1","minPartySize":1,"maxPartySize":4,"active":true}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.restaurantTable.label") { value("T1") }
        }.andReturn()
        val tableId = tableResponse.response.contentAsString.substringAfter("\"id\":").substringBefore(",").trim().toLong()

        mockMvc.post("/api/app/company/acme-bistro/restaurant-service-periods") {
            with(csrf().asHeader())
            this.session = adminSession
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "name":"Dinner",
                  "dayOfWeek":"FRIDAY",
                  "opensAt":"18:00",
                  "closesAt":"22:00",
                  "slotIntervalMinutes":30,
                  "reservationDurationMinutes":90,
                  "minPartySize":1,
                  "maxPartySize":4,
                  "bookingWindowDays":30,
                  "active":true
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.restaurantServicePeriod.name") { value("Dinner") }
        }

        val company = companyRepository.findBySlug("acme-bistro") ?: error("Expected company")
        val servicePeriod = restaurantServicePeriodRepository.findAllByCompanyIdOrderByDayOfWeekAscOpensAtAsc(requireNotNull(company.id)).first()
        val table = restaurantTableRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(company.id)).first()
        val reservation = seedReservation(company, servicePeriod, table)
        val staffSession = authenticatedSession(requireNotNull(staff.id), "staff@acme.com", "acme-bistro", CompanyRole.STAFF)

        mockMvc.get("/api/app/company/acme-bistro/restaurant-floorbook") {
            this.session = staffSession
            param("date", "2026-04-10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.entries.length()") { value(1) }
            jsonPath("$.entries[0].tableLabels[0]") { value("T1") }
        }

        mockMvc.put("/api/app/company/acme-bistro/restaurant-reservations/${requireNotNull(reservation.id)}/status") {
            with(csrf().asHeader())
            this.session = staffSession
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """{"status":"SEATED","tableIds":[$tableId]}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.restaurantReservation.status") { value("SEATED") }
        }
    }

    private fun seedReservation(company: Company, servicePeriod: RestaurantServicePeriod, table: RestaurantTable): RestaurantReservation {
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
                requestSummary = "Restaurant reservation"
            )
        )
        val reservation = restaurantReservationRepository.save(
            RestaurantReservation(
                booking = booking,
                company = company,
                servicePeriod = servicePeriod,
                reservedAt = Instant.parse("2026-04-10T18:00:00Z"),
                reservedUntil = Instant.parse("2026-04-10T19:30:00Z"),
                partySize = 2,
                status = RestaurantReservationStatus.CONFIRMED
            )
        )
        restaurantReservationTableRepository.save(RestaurantReservationTable(reservation = reservation, restaurantTable = table))
        return restaurantReservationRepository.findByIdAndCompanyId(requireNotNull(reservation.id), requireNotNull(company.id)) ?: error("Expected reservation")
    }

    private fun authenticatedSession(userId: Long, email: String, companySlug: String, role: CompanyRole): MockHttpSession {
        val principal = AppAuthenticatedUser(
            userId = userId,
            email = email,
            isPlatformAdmin = false,
            companySlug = companySlug,
            companyRole = role.name
        )
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority(if (role == CompanyRole.COMPANY_ADMIN) "ROLE_COMPANY_ADMIN" else "ROLE_COMPANY_STAFF"))
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        return MockHttpSession().apply {
            setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
            val nowMillis = System.currentTimeMillis()
            setAttribute(SessionSecurityAttributes.AUTHENTICATED_AT_MILLIS, nowMillis)
            setAttribute(SessionSecurityAttributes.LAST_SEEN_AT_MILLIS, nowMillis)
            setAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS, nowMillis)
        }
    }

    private fun seedCompanyUser(slug: String, email: String, role: CompanyRole): UserAccount {
        val company = companyRepository.findBySlug(slug)
            ?: companyRepository.save(
                Company(
                    name = "Acme Bistro",
                    businessType = BusinessType.RESTAURANT,
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
                passwordHash = passwordEncoder.encode("SecurePass123"),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )
        membershipRepository.save(CompanyMembership(company = company, user = user, role = role))
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
