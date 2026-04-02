package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.groupclass.domain.ClassBookingStatus
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassInstructorRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import java.time.LocalDate
import java.time.ZoneOffset
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class InstructorClassScheduleService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classInstructorRepository: ClassInstructorRepository,
    private val classSessionRepository: ClassSessionRepository,
    private val classBookingRepository: ClassBookingRepository
) {

    fun list(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<InstructorClassScheduleEntrySummary> {
        companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val instructor = classInstructorRepository.findFirstByCompanySlugAndLinkedUserId(requestedSlug, principal.userId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        val targetDate = try {
            LocalDate.parse(date)
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Date must be valid.")
        }
        val startsAt = targetDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endsAt = targetDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return classSessionRepository.findAllByInstructorIdAndStartsAtBetweenOrderByStartsAtAsc(instructor.id!!, startsAt, endsAt).map { session ->
            InstructorClassScheduleEntrySummary(
                sessionId = session.id!!,
                classTypeName = session.classType.name,
                startsAt = session.startsAt.toString(),
                endsAt = session.endsAt.toString(),
                confirmedCount = classBookingRepository.countByClassSessionIdAndStatusIn(session.id!!, occupiableStatuses()).toInt(),
                waitlistCount = classBookingRepository.countByClassSessionIdAndStatusIn(session.id!!, listOf(ClassBookingStatus.WAITLISTED)).toInt()
            )
        }
    }
}
