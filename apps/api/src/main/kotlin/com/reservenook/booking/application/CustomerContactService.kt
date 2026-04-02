package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.CustomerContact
import com.reservenook.booking.infrastructure.CustomerContactRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerContactSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CustomerContactService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val customerContactRepository: CustomerContactRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeCustomerContactSummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        return customerContactRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun create(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CompanyBackofficeCustomerContactSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val contact = mergeOrCreate(
            companyId = requireNotNull(company.id),
            company = company,
            fullName = fullName,
            email = email,
            phone = phone,
            preferredLanguage = preferredLanguage,
            notes = notes
        )

        securityAuditService.record(
            eventType = if (contact.createdAt == contact.updatedAt) SecurityAuditEventType.CUSTOMER_CONTACT_CREATED else SecurityAuditEventType.CUSTOMER_CONTACT_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = contact.email
        )
        return contact.toSummary()
    }

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        contactId: Long,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CompanyBackofficeCustomerContactSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val contact = customerContactRepository.findByIdAndCompanyId(contactId, requireNotNull(company.id))
            ?: throw IllegalArgumentException("Customer contact could not be found.")

        applyContactValues(contact, fullName, email, phone, preferredLanguage, notes)
        customerContactRepository.save(contact)

        securityAuditService.record(
            eventType = SecurityAuditEventType.CUSTOMER_CONTACT_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = contact.email
        )
        return contact.toSummary()
    }

    fun mergeOrCreate(
        companyId: Long,
        company: com.reservenook.registration.domain.Company,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CustomerContact {
        val normalizedEmail = CommonInputValidation.requireEmail(email, "Customer email must be a valid email address.")
        validateContact(fullName, phone, preferredLanguage, notes)

        val existing = customerContactRepository.findFirstByCompanyIdAndNormalizedEmail(companyId, normalizedEmail)
        val contact = existing ?: CustomerContact(
            company = company,
            fullName = fullName.trim(),
            email = normalizedEmail,
            normalizedEmail = normalizedEmail
        )

        applyContactValues(contact, fullName, normalizedEmail, phone, preferredLanguage, notes)
        return customerContactRepository.save(contact)
    }

    private fun applyContactValues(
        contact: CustomerContact,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ) {
        validateContact(fullName, phone, preferredLanguage, notes)
        val normalizedEmail = CommonInputValidation.requireEmail(email, "Customer email must be a valid email address.")
        contact.fullName = fullName.trim()
        contact.email = normalizedEmail
        contact.normalizedEmail = normalizedEmail
        contact.phone = phone?.trim()?.ifBlank { null }
        contact.preferredLanguage = preferredLanguage?.trim()?.lowercase()?.ifBlank { null }
        contact.notes = notes?.trim()?.ifBlank { null }
        contact.updatedAt = Instant.now()
    }

    private fun validateContact(fullName: String, phone: String?, preferredLanguage: String?, notes: String?) {
        if (fullName.trim().isBlank()) {
            throw IllegalArgumentException("Customer full name is required.")
        }
        CommonInputValidation.requireOptionalPhone(phone, "Customer phone must be a valid phone number.")
        if (preferredLanguage != null && preferredLanguage.trim().isNotBlank() && preferredLanguage.trim().lowercase() !in setOf("en", "de", "pt")) {
            throw IllegalArgumentException("Customer preferred language is not supported.")
        }
        if (notes != null && notes.length > 1000) {
            throw IllegalArgumentException("Customer notes must be 1000 characters or fewer.")
        }
    }
}
