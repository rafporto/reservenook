package com.reservenook.booking.application

import com.reservenook.booking.api.PublicBookingIntakeConfigResponse
import com.reservenook.booking.api.PublicBookingQuestionSummary
import com.reservenook.booking.api.SubmitPublicBookingIntakeRequest
import com.reservenook.booking.api.SubmitPublicBookingIntakeResponse
import com.reservenook.booking.domain.BookingSource
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PublicBookingIntakeService(
    private val companyRepository: CompanyRepository,
    private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun getConfig(slug: String): PublicBookingIntakeConfigResponse {
        val company = findPublicCompany(slug)
        return PublicBookingIntakeConfigResponse(
            companyName = company.name,
            companySlug = company.slug,
            displayName = company.brandDisplayName ?: company.name,
            defaultLanguage = company.defaultLanguage,
            defaultLocale = company.defaultLocale,
            ctaLabel = company.widgetCtaLabel ?: "Request booking",
            bookingEnabled = company.widgetEnabled,
            customerQuestions = companyCustomerQuestionRepository.findAllByCompanyIdOrderByDisplayOrderAsc(requireNotNull(company.id))
                .filter { it.enabled }
                .map {
                    PublicBookingQuestionSummary(
                        label = it.label,
                        questionType = it.questionType.name,
                        required = it.required,
                        options = it.optionsText?.split("\n")?.map(String::trim)?.filter(String::isNotBlank) ?: emptyList()
                    )
                }
        )
    }

    @Transactional
    fun submit(slug: String, clientAddress: String, request: SubmitPublicBookingIntakeRequest): SubmitPublicBookingIntakeResponse {
        val normalizedEmail = request.email.trim().lowercase()
        try {
            publicRequestAbuseGuard.assertAllowed("booking-intake", clientAddress, normalizedEmail)
        } catch (exception: Exception) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.PUBLIC_BOOKING_INTAKE_RATE_LIMITED,
                outcome = SecurityAuditOutcome.SUCCESS,
                actorEmail = normalizedEmail,
                companySlug = slug,
                targetEmail = normalizedEmail,
                details = clientAddress
            )
            throw exception
        }

        val company = findPublicCompany(slug)
        bookingInfrastructureService.createBookingInternal(
            companyId = requireNotNull(company.id),
            principal = null,
            fullName = request.fullName,
            email = normalizedEmail,
            phone = request.phone,
            preferredLanguage = request.preferredLanguage ?: company.defaultLanguage,
            requestSummary = request.requestSummary,
            preferredDateIso = request.preferredDate,
            notes = request.notes,
            source = BookingSource.PUBLIC_WEB,
            companySlug = company.slug,
            company = company
        )

        return SubmitPublicBookingIntakeResponse(
            message = "Your booking request has been received. The company will review it shortly."
        )
    }

    private fun findPublicCompany(slug: String) =
        companyRepository.findBySlug(slug)
            ?.takeIf { it.status == CompanyStatus.ACTIVE && it.widgetEnabled }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Booking is unavailable.")
}
