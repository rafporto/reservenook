package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeClassBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassInstructorSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassSessionSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassTypeSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class PublicClassSessionSummary(
    val sessionId: Long,
    val classTypeId: Long,
    val classTypeName: String,
    val instructorId: Long,
    val instructorName: String,
    val startsAt: String,
    val endsAt: String,
    val remainingCapacity: Int,
    val waitlistOpen: Boolean
)

data class InstructorClassScheduleEntrySummary(
    val sessionId: Long,
    val classTypeName: String,
    val startsAt: String,
    val endsAt: String,
    val confirmedCount: Int,
    val waitlistCount: Int
)

@Service
class GroupClassConfigurationService(
    private val classTypeManagementService: ClassTypeManagementService,
    private val classInstructorManagementService: ClassInstructorManagementService,
    private val classSessionManagementService: ClassSessionManagementService,
    private val publicClassAvailabilityService: PublicClassAvailabilityService,
    private val publicClassBookingService: PublicClassBookingService,
    private val classBookingOutcomeService: ClassBookingOutcomeService,
    private val instructorClassScheduleService: InstructorClassScheduleService
) {

    @Transactional(readOnly = true)
    fun listClassTypes(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassTypeSummary> =
        classTypeManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertClassType(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        classTypeId: Long?,
        name: String,
        description: String?,
        durationMinutes: Int,
        defaultCapacity: Int,
        active: Boolean,
        autoConfirm: Boolean
    ): CompanyBackofficeClassTypeSummary =
        classTypeManagementService.upsert(principal, requestedSlug, classTypeId, name, description, durationMinutes, defaultCapacity, active, autoConfirm)

    @Transactional(readOnly = true)
    fun listClassInstructors(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassInstructorSummary> =
        classInstructorManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertClassInstructor(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        instructorId: Long?,
        linkedUserId: Long?,
        displayName: String,
        email: String?,
        active: Boolean
    ): CompanyBackofficeClassInstructorSummary =
        classInstructorManagementService.upsert(principal, requestedSlug, instructorId, linkedUserId, displayName, email, active)

    @Transactional(readOnly = true)
    fun listClassSessions(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassSessionSummary> =
        classSessionManagementService.listSessions(principal, requestedSlug)

    @Transactional
    fun upsertClassSession(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        sessionId: Long?,
        classTypeId: Long,
        instructorId: Long,
        startsAt: String,
        endsAt: String,
        capacity: Int,
        status: String
    ): CompanyBackofficeClassSessionSummary =
        classSessionManagementService.upsertSession(principal, requestedSlug, sessionId, classTypeId, instructorId, startsAt, endsAt, capacity, status)

    @Transactional(readOnly = true)
    fun listClassBookings(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassBookingSummary> =
        classBookingOutcomeService.list(principal, requestedSlug)

    @Transactional
    fun updateClassBookingOutcome(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        classBookingId: Long,
        status: String
    ): CompanyBackofficeClassBookingSummary =
        classBookingOutcomeService.update(principal, requestedSlug, classBookingId, status)

    @Transactional(readOnly = true)
    fun getPublicAvailability(slug: String, classTypeId: Long, clientAddress: String): List<PublicClassSessionSummary> =
        publicClassAvailabilityService.getAvailability(slug, classTypeId, clientAddress)

    @Transactional
    fun bookPublicClass(
        slug: String,
        clientAddress: String,
        sessionId: Long,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?
    ): CompanyBackofficeClassBookingSummary =
        publicClassBookingService.book(slug, clientAddress, sessionId, fullName, email, phone, preferredLanguage)

    @Transactional(readOnly = true)
    fun listInstructorSchedule(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<InstructorClassScheduleEntrySummary> =
        instructorClassScheduleService.list(principal, requestedSlug, date)
}
