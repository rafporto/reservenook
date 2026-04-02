package com.reservenook.booking.api

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.domain.AppointmentProviderAvailability
import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.appointment.infrastructure.AppointmentBookingRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderAvailabilityRepository
import com.reservenook.appointment.infrastructure.AppointmentProviderRepository
import com.reservenook.appointment.infrastructure.AppointmentServiceRepository
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.companybackoffice.domain.CompanyCustomerQuestion
import com.reservenook.companybackoffice.domain.CustomerQuestionType
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.application.RegistrationMailSender
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.RequestThrottleService
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
class PublicBookingControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    @Autowired private val customerContactRepository: CustomerContactRepository,
    @Autowired private val bookingRepository: BookingRepository,
    @Autowired private val bookingAuditEventRepository: BookingAuditEventRepository,
    @Autowired private val appointmentBookingRepository: AppointmentBookingRepository,
    @Autowired private val appointmentServiceRepository: AppointmentServiceRepository,
    @Autowired private val appointmentProviderRepository: AppointmentProviderRepository,
    @Autowired private val appointmentProviderAvailabilityRepository: AppointmentProviderAvailabilityRepository,
    @Autowired private val requestThrottleService: RequestThrottleService
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
        bookingAuditEventRepository.deleteAll()
        bookingRepository.deleteAll()
        customerContactRepository.deleteAll()
        companyCustomerQuestionRepository.deleteAll()
        companyRepository.deleteAll()
    }

    @Test
    fun `public booking config returns enabled tenant booking page`() {
        val company = seedPublicBookingCompany(slug = "acme-booking", enabled = true)
        companyCustomerQuestionRepository.save(
            CompanyCustomerQuestion(
                company = company,
                label = "Preferred provider",
                questionType = CustomerQuestionType.SINGLE_SELECT,
                required = true,
                enabled = true,
                displayOrder = 0,
                optionsText = "Any\nAnna"
            )
        )

        mockMvc.get("/api/public/companies/acme-booking/booking-intake-config")
            .andExpect {
                status { isOk() }
                jsonPath("$.companySlug") { value("acme-booking") }
                jsonPath("$.businessType") { value("APPOINTMENT") }
                jsonPath("$.customerQuestions[0].label") { value("Preferred provider") }
                jsonPath("$.appointmentServices.length()") { value(0) }
            }
    }

    @Test
    fun `public booking intake creates tenant scoped contact booking and audit`() {
        seedPublicBookingCompany(slug = "acme-booking", enabled = true)

        mockMvc.post("/api/public/companies/acme-booking/booking-intake") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "fullName":"Alex Guest",
                  "email":"alex@example.com",
                  "phone":"+49 30 111 2222",
                  "preferredLanguage":"en",
                  "requestSummary":"Intro booking request",
                  "preferredDate":"2026-04-08",
                  "notes":"Please contact by email."
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Your booking request has been received. The company will review it shortly.") }
        }

        org.junit.jupiter.api.Assertions.assertEquals(1, customerContactRepository.count())
        org.junit.jupiter.api.Assertions.assertEquals(1, bookingRepository.count())
        org.junit.jupiter.api.Assertions.assertEquals(1, bookingAuditEventRepository.count())
    }

    @Test
    fun `public booking intake returns not found for disabled booking page`() {
        seedPublicBookingCompany(slug = "disabled-booking", enabled = false)

        mockMvc.get("/api/public/companies/disabled-booking/booking-intake-config")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `public booking intake rate limits repeated abuse`() {
        seedPublicBookingCompany(slug = "acme-booking", enabled = true)

        repeat(5) {
            mockMvc.post("/api/public/companies/acme-booking/booking-intake") {
                contentType = org.springframework.http.MediaType.APPLICATION_JSON
                content = """
                    {
                      "fullName":"Alex Guest",
                      "email":"alex@example.com",
                      "phone":null,
                      "preferredLanguage":"en",
                      "requestSummary":"Intro booking request",
                      "preferredDate":"2026-04-08",
                      "notes":null
                    }
                """.trimIndent()
            }.andExpect {
                status { isOk() }
            }
        }

        mockMvc.post("/api/public/companies/acme-booking/booking-intake") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "fullName":"Alex Guest",
                  "email":"alex@example.com",
                  "phone":null,
                  "preferredLanguage":"en",
                  "requestSummary":"Intro booking request",
                  "preferredDate":"2026-04-08",
                  "notes":null
                }
            """.trimIndent()
        }.andExpect {
            status { isTooManyRequests() }
        }
    }

    @Test
    fun `public appointment availability returns enabled service slots`() {
        val company = seedPublicBookingCompany(slug = "acme-booking", enabled = true)
        val service = appointmentServiceRepository.save(
            AppointmentService(
                company = company,
                name = "Initial consultation",
                durationMinutes = 30,
                bufferMinutes = 0,
                enabled = true
            )
        )
        val provider = appointmentProviderRepository.save(
            AppointmentProvider(
                company = company,
                displayName = "Anna Therapist",
                email = "anna@acme.com"
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

        mockMvc.get("/api/public/companies/acme-booking/appointments/availability") {
            param("serviceId", requireNotNull(service.id).toString())
            param("date", "2026-04-10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.slots.length()") { value(6) }
            jsonPath("$.slots[0].providerName") { value("Anna Therapist") }
        }
    }

    @Test
    fun `public appointment booking creates tenant scoped appointment record`() {
        val company = seedPublicBookingCompany(slug = "acme-booking", enabled = true)
        val service = appointmentServiceRepository.save(
            AppointmentService(
                company = company,
                name = "Initial consultation",
                durationMinutes = 30,
                bufferMinutes = 10,
                enabled = true,
                autoConfirm = true
            )
        )
        val provider = appointmentProviderRepository.save(
            AppointmentProvider(
                company = company,
                displayName = "Anna Therapist",
                email = "anna@acme.com"
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

        mockMvc.post("/api/public/companies/acme-booking/appointments/book") {
            contentType = org.springframework.http.MediaType.APPLICATION_JSON
            content = """
                {
                  "fullName":"Alex Guest",
                  "email":"alex@example.com",
                  "phone":"+49 30 111 2222",
                  "preferredLanguage":"en",
                  "serviceId":${requireNotNull(service.id)},
                  "providerId":${requireNotNull(provider.id)},
                  "startsAt":"2026-04-10T09:00:00Z"
                }
            """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.booking.status") { value("CONFIRMED") }
            jsonPath("$.booking.customerEmail") { value("alex@example.com") }
        }

        org.junit.jupiter.api.Assertions.assertEquals(1, appointmentBookingRepository.count())
    }

    private fun seedPublicBookingCompany(slug: String, enabled: Boolean): Company =
        companyRepository.save(
            Company(
                name = "Acme Booking",
                businessType = BusinessType.APPOINTMENT,
                slug = slug,
                status = CompanyStatus.ACTIVE,
                defaultLanguage = "en",
                defaultLocale = "en-US",
                widgetEnabled = enabled,
                widgetCtaLabel = "Reserve now"
            )
        )
}
