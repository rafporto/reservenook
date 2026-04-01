package com.reservenook.booking.api

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
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

@SpringBootTest
@AutoConfigureMockMvc
class PublicBookingControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val companyRepository: CompanyRepository,
    @Autowired private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    @Autowired private val customerContactRepository: CustomerContactRepository,
    @Autowired private val bookingRepository: BookingRepository,
    @Autowired private val bookingAuditEventRepository: BookingAuditEventRepository,
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
                jsonPath("$.customerQuestions[0].label") { value("Preferred provider") }
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
