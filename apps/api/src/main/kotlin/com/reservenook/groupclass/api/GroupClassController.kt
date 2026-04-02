package com.reservenook.groupclass.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.ClassBookingsResponse
import com.reservenook.companybackoffice.api.ClassInstructorsResponse
import com.reservenook.companybackoffice.api.ClassSessionsResponse
import com.reservenook.companybackoffice.api.ClassTypesResponse
import com.reservenook.companybackoffice.api.UpdateClassBookingOutcomeRequest
import com.reservenook.companybackoffice.api.UpdateClassBookingOutcomeResponse
import com.reservenook.companybackoffice.api.UpsertClassInstructorRequest
import com.reservenook.companybackoffice.api.UpsertClassInstructorResponse
import com.reservenook.companybackoffice.api.UpsertClassSessionRequest
import com.reservenook.companybackoffice.api.UpsertClassSessionResponse
import com.reservenook.companybackoffice.api.UpsertClassTypeRequest
import com.reservenook.companybackoffice.api.UpsertClassTypeResponse
import com.reservenook.groupclass.application.GroupClassConfigurationService
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class GroupClassController(
    private val groupClassConfigurationService: GroupClassConfigurationService,
    private val recentAuthenticationGuard: com.reservenook.security.application.RecentAuthenticationGuard
) {

    @GetMapping("/api/app/company/{slug}/class-types")
    fun listClassTypes(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = ClassTypesResponse(groupClassConfigurationService.listClassTypes(principal, slug))

    @PostMapping("/api/app/company/{slug}/class-types")
    fun createClassType(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassTypeRequest
    ) = UpsertClassTypeResponse(
        message = "Class type saved.",
        classType = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassType(
                principal, slug, null, request.name, request.description, request.durationMinutes, request.defaultCapacity, request.active, request.autoConfirm
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/class-types/{classTypeId}")
    fun updateClassType(
        @PathVariable slug: String,
        @PathVariable classTypeId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassTypeRequest
    ) = UpsertClassTypeResponse(
        message = "Class type updated.",
        classType = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassType(
                principal, slug, classTypeId, request.name, request.description, request.durationMinutes, request.defaultCapacity, request.active, request.autoConfirm
            )
        }
    )

    @GetMapping("/api/app/company/{slug}/class-instructors")
    fun listClassInstructors(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = ClassInstructorsResponse(groupClassConfigurationService.listClassInstructors(principal, slug))

    @PostMapping("/api/app/company/{slug}/class-instructors")
    fun createClassInstructor(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassInstructorRequest
    ) = UpsertClassInstructorResponse(
        message = "Class instructor saved.",
        classInstructor = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassInstructor(
                principal, slug, null, request.linkedUserId, request.displayName, request.email, request.active
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/class-instructors/{instructorId}")
    fun updateClassInstructor(
        @PathVariable slug: String,
        @PathVariable instructorId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassInstructorRequest
    ) = UpsertClassInstructorResponse(
        message = "Class instructor updated.",
        classInstructor = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassInstructor(
                principal, slug, instructorId, request.linkedUserId, request.displayName, request.email, request.active
            )
        }
    )

    @GetMapping("/api/app/company/{slug}/class-sessions")
    fun listClassSessions(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = ClassSessionsResponse(groupClassConfigurationService.listClassSessions(principal, slug))

    @PostMapping("/api/app/company/{slug}/class-sessions")
    fun createClassSession(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassSessionRequest
    ) = UpsertClassSessionResponse(
        message = "Class session saved.",
        classSession = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassSession(
                principal, slug, null, request.classTypeId, request.instructorId, request.startsAt, request.endsAt, request.capacity, request.status
            )
        }
    )

    @PutMapping("/api/app/company/{slug}/class-sessions/{sessionId}")
    fun updateClassSession(
        @PathVariable slug: String,
        @PathVariable sessionId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertClassSessionRequest
    ) = UpsertClassSessionResponse(
        message = "Class session updated.",
        classSession = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            groupClassConfigurationService.upsertClassSession(
                principal, slug, sessionId, request.classTypeId, request.instructorId, request.startsAt, request.endsAt, request.capacity, request.status
            )
        }
    )

    @GetMapping("/api/app/company/{slug}/class-bookings")
    fun listClassBookings(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser
    ) = ClassBookingsResponse(groupClassConfigurationService.listClassBookings(principal, slug))

    @PutMapping("/api/app/company/{slug}/class-bookings/{classBookingId}/status")
    fun updateClassBookingOutcome(
        @PathVariable slug: String,
        @PathVariable classBookingId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestBody request: UpdateClassBookingOutcomeRequest
    ) = UpdateClassBookingOutcomeResponse(
        message = "Class booking updated.",
        classBooking = groupClassConfigurationService.updateClassBookingOutcome(principal, slug, classBookingId, request.status)
    )

    @GetMapping("/api/app/company/{slug}/class-instructors/me/sessions")
    fun myClassSessions(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestParam date: String
    ) = InstructorClassScheduleResponse(groupClassConfigurationService.listInstructorSchedule(principal, slug, date))
}

data class InstructorClassScheduleResponse(
    val entries: List<com.reservenook.groupclass.application.InstructorClassScheduleEntrySummary>
)
