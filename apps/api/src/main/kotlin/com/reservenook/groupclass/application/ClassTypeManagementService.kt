package com.reservenook.groupclass.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeClassTypeSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.groupclass.domain.ClassType
import com.reservenook.groupclass.infrastructure.ClassTypeRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class ClassTypeManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val classTypeRepository: ClassTypeRepository,
    private val securityAuditService: SecurityAuditService
) {

    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeClassTypeSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return classTypeRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    fun upsert(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        classTypeId: Long?,
        name: String,
        description: String?,
        durationMinutes: Int,
        defaultCapacity: Int,
        active: Boolean,
        autoConfirm: Boolean
    ): CompanyBackofficeClassTypeSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val now = Instant.now()
        val entity = if (classTypeId == null) {
            ClassType(
                company = company,
                name = requireClassName(name),
                description = description?.trim()?.ifBlank { null },
                durationMinutes = requirePositiveDuration(durationMinutes),
                defaultCapacity = requirePositiveCapacity(defaultCapacity),
                active = active,
                autoConfirm = autoConfirm,
                createdAt = now,
                updatedAt = now
            )
        } else {
            classTypeRepository.findByIdAndCompanyId(classTypeId, companyId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Class type could not be found.")
        }

        entity.name = requireClassName(name)
        entity.description = description?.trim()?.ifBlank { null }
        entity.durationMinutes = requirePositiveDuration(durationMinutes)
        entity.defaultCapacity = requirePositiveCapacity(defaultCapacity)
        entity.active = active
        entity.autoConfirm = autoConfirm
        entity.updatedAt = now
        val saved = classTypeRepository.save(entity)
        securityAuditService.record(
            eventType = SecurityAuditEventType.CLASS_TYPE_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = requestedSlug,
            details = saved.name
        )
        return saved.toSummary()
    }
}
