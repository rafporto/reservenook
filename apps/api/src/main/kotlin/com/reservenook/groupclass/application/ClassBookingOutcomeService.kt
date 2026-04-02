package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.companybackoffice.api.CompanyBackofficeClassBookingSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ClassBookingOutcomeService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classBookingRepository: ClassBookingRepository,
    private val classSessionRepository: ClassSessionRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassBookingSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return classBookingRepository.findAllByCompanyIdOrderByCreatedAtDesc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun update(principal: AppAuthenticatedUser, requestedSlug: String, classBookingId: Long, status: String): CompanyBackofficeClassBookingSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val companyId = requireNotNull(membership.company.id)
        val classBooking = classBookingRepository.findById(classBookingId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Class booking could not be found.")
        }
        if (classBooking.company.id != companyId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
        val nextStatus = try {
            ClassBookingStatus.valueOf(status.trim().uppercase())
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class booking status is invalid.")
        }
        classBooking.status = nextStatus
        classBooking.updatedAt = Instant.now()
        classBooking.booking.status = when (nextStatus) {
            ClassBookingStatus.CONFIRMED -> BookingStatus.CONFIRMED
            ClassBookingStatus.WAITLISTED -> BookingStatus.PENDING
            ClassBookingStatus.CANCELLED -> BookingStatus.CANCELLED
            ClassBookingStatus.ATTENDED -> BookingStatus.COMPLETED
            ClassBookingStatus.NO_SHOW -> BookingStatus.NO_SHOW
        }
        if (nextStatus == ClassBookingStatus.CANCELLED) {
            classBooking.waitlistPosition = null
        }
        val saved = classBookingRepository.save(classBooking)
        if (nextStatus == ClassBookingStatus.CANCELLED) {
            promoteWaitlist(saved.classSession.id!!)
        }
        securityAuditService.record(
            eventType = SecurityAuditEventType.CLASS_BOOKING_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = requestedSlug,
            details = "${saved.id}:${nextStatus.name}"
        )
        return saved.toSummary()
    }

    private fun promoteWaitlist(classSessionId: Long) {
        classSessionRepository.findWithLockById(classSessionId) ?: return
        val promoted = classBookingRepository.findFirstByClassSessionIdAndStatusOrderByWaitlistPositionAsc(
            classSessionId,
            ClassBookingStatus.WAITLISTED
        ) ?: return
        promoted.status = ClassBookingStatus.CONFIRMED
        promoted.waitlistPosition = null
        promoted.updatedAt = Instant.now()
        promoted.booking.status = BookingStatus.CONFIRMED
        classBookingRepository.save(promoted)
    }
}
