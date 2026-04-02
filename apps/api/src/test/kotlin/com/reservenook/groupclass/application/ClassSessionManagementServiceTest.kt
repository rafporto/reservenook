package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.groupclass.infrastructure.ClassBookingRepository
import com.reservenook.groupclass.infrastructure.ClassInstructorRepository
import com.reservenook.groupclass.infrastructure.ClassSessionRepository
import com.reservenook.groupclass.infrastructure.ClassTypeRepository
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.security.application.SecurityAuditService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

class ClassSessionManagementServiceTest {

    private val companyAdminAccessService = mockk<CompanyAdminAccessService>()
    private val classTypeRepository = mockk<ClassTypeRepository>()
    private val classInstructorRepository = mockk<ClassInstructorRepository>()
    private val classSessionRepository = mockk<ClassSessionRepository>(relaxed = true)
    private val classBookingRepository = mockk<ClassBookingRepository>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)

    private val service = ClassSessionManagementService(
        companyAdminAccessService,
        classTypeRepository,
        classInstructorRepository,
        classSessionRepository,
        classBookingRepository,
        securityAuditService
    )

    @Test
    fun `upsert session rejects overlapping instructor sessions`() {
        val membership = adminMembership()
        val company = membership.company
        val classType = com.reservenook.groupclass.domain.ClassType(
            id = 11L,
            company = company,
            name = "Morning Yoga",
            durationMinutes = 60,
            defaultCapacity = 10
        )
        val instructor = com.reservenook.groupclass.domain.ClassInstructor(
            id = 22L,
            company = company,
            displayName = "Lena Coach"
        )

        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-classes") } returns membership
        every { classTypeRepository.findByIdAndCompanyId(11L, 1L) } returns classType
        every { classInstructorRepository.findByIdAndCompanyId(22L, 1L) } returns instructor
        every {
            classSessionRepository.existsByInstructorIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
                22L,
                com.reservenook.groupclass.domain.ClassSessionStatus.SCHEDULED,
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-10T09:00:00Z")
            )
        } returns true

        val exception = assertThrows(ResponseStatusException::class.java) {
            service.upsertSession(
                principal = principal(),
                requestedSlug = "acme-classes",
                sessionId = null,
                classTypeId = 11L,
                instructorId = 22L,
                startsAt = "2026-04-10T09:00:00Z",
                endsAt = "2026-04-10T10:00:00Z",
                capacity = 12,
                status = "SCHEDULED"
            )
        }

        assertEquals("400 BAD_REQUEST \"The instructor already has an overlapping class session.\"", exception.message)
    }

    private fun principal() = AppAuthenticatedUser(
        userId = 5L,
        email = "admin@acme.com",
        isPlatformAdmin = false,
        companySlug = "acme-classes",
        companyRole = CompanyRole.COMPANY_ADMIN.name
    )

    private fun adminMembership(): CompanyMembership {
        val company = Company(
            id = 1L,
            name = "Acme Classes",
            businessType = BusinessType.CLASS,
            slug = "acme-classes",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val user = UserAccount(
            id = 5L,
            email = "admin@acme.com",
            fullName = "Admin",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true
        )
        return CompanyMembership(
            id = 9L,
            company = company,
            user = user,
            role = CompanyRole.COMPANY_ADMIN
        )
    }
}
