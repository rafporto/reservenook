package com.reservenook.groupclass.application

import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.domain.ClassSessionStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.groupclass.infrastructure.ClassTypeRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class PublicClassAvailabilityService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classTypeRepository: ClassTypeRepository,
    private val classSessionRepository: ClassSessionRepository,
    private val classBookingRepository: ClassBookingRepository,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard
) {

    fun getAvailability(slug: String, classTypeId: Long, clientAddress: String): List<PublicClassSessionSummary> {
        publicRequestAbuseGuard.assertAllowed("class-availability", clientAddress, slug)
        val company = companyAdminAccessService.requireActivePublicCompany(slug)
        if (company.businessType.name != "CLASS") {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Booking is unavailable.")
        }
        val classType = classTypeRepository.findByIdAndCompanyId(classTypeId, requireNotNull(company.id))
            ?.takeIf { it.active }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class availability is unavailable.")
        val sessions = classSessionRepository.findAllByCompanyIdAndClassTypeIdAndStatusAndStartsAtGreaterThanEqualOrderByStartsAtAsc(
            requireNotNull(company.id),
            requireNotNull(classType.id),
            ClassSessionStatus.SCHEDULED,
            Instant.now()
        )
        val sessionCounts = classBookingRepository.summarizeByClassSessionIds(sessions.mapNotNull { it.id })
            .groupBy { it.classSessionId }
        return sessions.map { session ->
            val counts = sessionCounts[session.id!!].orEmpty()
            val confirmedCount = counts
                .filter { it.status in occupiableStatuses() }
                .sumOf { it.total }
                .toInt()
            val waitlistCount = counts
                .filter { it.status == ClassBookingStatus.WAITLISTED }
                .sumOf { it.total }
                .toInt()
            val remainingCapacity = (session.capacity - confirmedCount).coerceAtLeast(0)
            PublicClassSessionSummary(
                sessionId = session.id!!,
                classTypeId = classType.id!!,
                classTypeName = classType.name,
                instructorId = session.instructor.id!!,
                instructorName = session.instructor.displayName,
                startsAt = session.startsAt.toString(),
                endsAt = session.endsAt.toString(),
                remainingCapacity = remainingCapacity,
                waitlistOpen = waitlistCount >= 0
            )
        }
    }
}
