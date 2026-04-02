package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeClassSessionSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.domain.ClassSession
import com.reservenook.groupclass.domain.ClassSessionStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassInstructorRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.groupclass.infrastructure.ClassTypeRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ClassSessionManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classTypeRepository: ClassTypeRepository,
    private val classInstructorRepository: ClassInstructorRepository,
    private val classSessionRepository: ClassSessionRepository,
    private val classBookingRepository: ClassBookingRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun listSessions(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassSessionSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return classSessionRepository.findAllByCompanyIdOrderByStartsAtAsc(requireNotNull(membership.company.id)).map { session ->
            session.toSummary(
                confirmedCount = classBookingRepository.countByClassSessionIdAndStatusIn(session.id!!, occupiableStatuses()).toInt(),
                waitlistCount = classBookingRepository.countByClassSessionIdAndStatusIn(session.id!!, listOf(ClassBookingStatus.WAITLISTED)).toInt()
            )
        }
    }

    fun upsertSession(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        sessionId: Long?,
        classTypeId: Long,
        instructorId: Long,
        startsAt: String,
        endsAt: String,
        capacity: Int,
        status: String
    ): CompanyBackofficeClassSessionSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val classType = classTypeRepository.findByIdAndCompanyId(classTypeId, companyId)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class type could not be found.")
        val instructor = classInstructorRepository.findByIdAndCompanyId(instructorId, companyId)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class instructor could not be found.")
        val startsAtInstant = parseRequiredInstant(startsAt, "Class session start time")
        val endsAtInstant = parseRequiredInstant(endsAt, "Class session end time")
        validateSessionWindow(startsAtInstant, endsAtInstant)
        val normalizedStatus = try {
            ClassSessionStatus.valueOf(status.trim().uppercase())
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class session status is invalid.")
        }
        ensureNoInstructorOverlap(instructor.id!!, sessionId, startsAtInstant, endsAtInstant, normalizedStatus)
        val now = Instant.now()
        val entity = if (sessionId == null) {
            ClassSession(
                company = company,
                classType = classType,
                instructor = instructor,
                startsAt = startsAtInstant,
                endsAt = endsAtInstant,
                capacity = requirePositiveCapacity(capacity),
                status = normalizedStatus,
                createdAt = now,
                updatedAt = now
            )
        } else {
            classSessionRepository.findByIdAndCompanyId(sessionId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class session could not be found.")
        }
        entity.classType = classType
        entity.instructor = instructor
        entity.startsAt = startsAtInstant
        entity.endsAt = endsAtInstant
        entity.capacity = requirePositiveCapacity(capacity)
        entity.status = normalizedStatus
        entity.updatedAt = now
        val saved = classSessionRepository.save(entity)
        securityAuditService.record(
            eventType = SecurityAuditEventType.CLASS_SESSION_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = requestedSlug,
            details = "${classType.name}:${saved.startsAt}"
        )
        return saved.toSummary(
            confirmedCount = classBookingRepository.countByClassSessionIdAndStatusIn(saved.id!!, occupiableStatuses()).toInt(),
            waitlistCount = classBookingRepository.countByClassSessionIdAndStatusIn(saved.id!!, listOf(ClassBookingStatus.WAITLISTED)).toInt()
        )
    }

    private fun ensureNoInstructorOverlap(
        instructorId: Long,
        sessionId: Long?,
        startsAt: Instant,
        endsAt: Instant,
        status: ClassSessionStatus
    ) {
        if (status == ClassSessionStatus.CANCELLED) {
            return
        }
        val overlaps = if (sessionId == null) {
            classSessionRepository.existsByInstructorIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
                instructorId,
                ClassSessionStatus.SCHEDULED,
                endsAt,
                startsAt
            )
        } else {
            classSessionRepository.existsByInstructorIdAndIdNotAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
                instructorId,
                sessionId,
                ClassSessionStatus.SCHEDULED,
                endsAt,
                startsAt
            )
        }
        if (overlaps) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The instructor already has an overlapping class session.")
        }
    }
}

internal fun occupiableStatuses(): List<ClassBookingStatus> =
    listOf(ClassBookingStatus.CONFIRMED, ClassBookingStatus.ATTENDED, ClassBookingStatus.NO_SHOW)
