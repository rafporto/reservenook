package com.reservenook.groupclass.application

import com.reservenook.booking.application.BookingInfrastructureService
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.booking.infrastructure.CustomerContactRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeClassBookingSummary
import com.reservenook.companybackoffice.application.supportedCompanyLanguages
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.groupclass.domain.ClassBooking
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.domain.ClassSessionStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class PublicClassBookingService(
    private val bookingInfrastructureService: BookingInfrastructureService,
    private val classSessionRepository: ClassSessionRepository,
    private val classBookingRepository: ClassBookingRepository,
    private val customerContactRepository: CustomerContactRepository,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun book(
        slug: String,
        clientAddress: String,
        sessionId: Long,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?
    ): CompanyBackofficeClassBookingSummary {
        val normalizedEmail = email.trim().lowercase()
        publicRequestAbuseGuard.assertAllowed("class-booking", clientAddress, normalizedEmail)
        val session = classSessionRepository.findWithLockById(sessionId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class session could not be found.")
        val company = session.company
        if (company.slug != slug || company.status.name != "ACTIVE" || company.businessType.name != "CLASS" || session.status != ClassSessionStatus.SCHEDULED) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class booking is unavailable.")
        }
        if (session.startsAt.isBefore(Instant.now())) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "This class session can no longer be booked.")
        }
        val existingContact = customerContactRepository.findFirstByCompanyIdAndNormalizedEmail(requireNotNull(company.id), normalizedEmail)
        if (existingContact != null && classBookingRepository.findFirstByClassSessionIdAndBookingCustomerContactIdAndStatusIn(
                sessionId,
                existingContact.id!!,
                listOf(ClassBookingStatus.CONFIRMED, ClassBookingStatus.WAITLISTED, ClassBookingStatus.ATTENDED, ClassBookingStatus.NO_SHOW)
            ) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "This customer already has a booking for the selected class session.")
        }
        val booking = bookingInfrastructureService.createBookingInternal(
            companyId = requireNotNull(company.id),
            principal = null,
            fullName = fullName,
            email = normalizedEmail,
            phone = phone,
            preferredLanguage = preferredLanguage?.takeIf { it in supportedCompanyLanguages() } ?: company.defaultLanguage,
            requestSummary = "Class booking: ${session.classType.name}",
            preferredDateIso = session.startsAt.toString().substringBefore("T"),
            notes = null,
            source = BookingSource.PUBLIC_WEB,
            companySlug = company.slug,
            company = company
        )
        val bookingEntity = bookingInfrastructureService.findBookingEntity(booking.id)
        val confirmedCount = classBookingRepository.countByClassSessionIdAndStatusIn(sessionId, occupiableStatuses()).toInt()
        val enrollmentStatus = if (confirmedCount < session.capacity) {
            bookingEntity.status = BookingStatus.CONFIRMED
            ClassBookingStatus.CONFIRMED
        } else {
            bookingEntity.status = BookingStatus.PENDING
            ClassBookingStatus.WAITLISTED
        }
        val waitlistPosition = if (enrollmentStatus == ClassBookingStatus.WAITLISTED) {
            classBookingRepository.countByClassSessionIdAndStatusIn(sessionId, listOf(ClassBookingStatus.WAITLISTED)).toInt() + 1
        } else {
            null
        }
        val saved = classBookingRepository.save(
            ClassBooking(
                booking = bookingEntity,
                company = company,
                classSession = session,
                status = enrollmentStatus,
                waitlistPosition = waitlistPosition,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.CLASS_BOOKED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorEmail = normalizedEmail,
            companySlug = slug,
            details = "${session.classType.name}:${session.startsAt}:${enrollmentStatus.name}"
        )
        return saved.toSummary()
    }
}
