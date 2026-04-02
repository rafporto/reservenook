package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeClassInstructorSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.groupclass.domain.ClassInstructor
import com.reservenook.groupclass.infrastructure.ClassInstructorRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ClassInstructorManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classInstructorRepository: ClassInstructorRepository,
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassInstructorSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return classInstructorRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        instructorId: Long?,
        linkedUserId: Long?,
        displayName: String,
        email: String?,
        active: Boolean
    ): CompanyBackofficeClassInstructorSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val linkedUser = linkedUserId?.let {
            val user = userAccountRepository.findById(it).orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Linked user could not be found.")
            }
            val linkedMembership = companyMembershipRepository.findFirstByUserIdAndCompanySlug(it, requestedSlug)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Linked user must belong to the same company.")
            linkedMembership.user
        }
        val now = Instant.now()
        val entity = if (instructorId == null) {
            ClassInstructor(
                company = company,
                linkedUser = linkedUser,
                displayName = requireInstructorName(displayName),
                email = normalizeOptionalEmail(email),
                active = active,
                createdAt = now,
                updatedAt = now
            )
        } else {
            classInstructorRepository.findByIdAndCompanyId(instructorId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class instructor could not be found.")
        }

        entity.linkedUser = linkedUser
        entity.displayName = requireInstructorName(displayName)
        entity.email = normalizeOptionalEmail(email)
        entity.active = active
        entity.updatedAt = now
        val saved = classInstructorRepository.save(entity)
        securityAuditService.record(
            eventType = SecurityAuditEventType.CLASS_INSTRUCTOR_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = requestedSlug,
            details = saved.displayName
        )
        return saved.toSummary()
    }
}
