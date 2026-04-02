package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerQuestionSummary
import com.reservenook.companybackoffice.domain.CompanyCustomerQuestion
import com.reservenook.companybackoffice.domain.CustomerQuestionType
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CompanyCustomerQuestionService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyCustomerQuestionRepository: CompanyCustomerQuestionRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<CustomerQuestionDraft>
    ): List<CompanyBackofficeCustomerQuestionSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map { entry ->
            val questionType = CompanyConfigurationSupport.parseQuestionType(entry.questionType)
            val label = entry.label.trim()
            if (label.isBlank()) {
                throw IllegalArgumentException("Question label is required.")
            }
            val options = entry.options.map { it.trim() }.filter { it.isNotBlank() }
            if (questionType == CustomerQuestionType.SINGLE_SELECT && options.size < 2) {
                throw IllegalArgumentException("Selectable questions require at least two options.")
            }
            if (questionType != CustomerQuestionType.SINGLE_SELECT && options.isNotEmpty()) {
                throw IllegalArgumentException("Only selectable questions can define options.")
            }
            NormalizedCustomerQuestionDraft(label, questionType, entry.required, entry.enabled, entry.displayOrder, options)
        }.sortedBy { it.displayOrder }

        companyCustomerQuestionRepository.deleteAllByCompanyId(companyId)
        val saved = companyCustomerQuestionRepository.saveAll(
            normalizedEntries.map {
                CompanyCustomerQuestion(
                    company = company,
                    label = it.label,
                    questionType = it.questionType,
                    required = it.required,
                    enabled = it.enabled,
                    displayOrder = it.displayOrder,
                    optionsText = it.options.takeIf { options -> options.isNotEmpty() }?.joinToString("\n")
                )
            }
        )

        securityAuditService.record(
            eventType = SecurityAuditEventType.COMPANY_CUSTOMER_QUESTIONS_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug
        )
        return saved.map { it.toSummary() }
    }
}

private data class NormalizedCustomerQuestionDraft(
    val label: String,
    val questionType: CustomerQuestionType,
    val required: Boolean,
    val enabled: Boolean,
    val displayOrder: Int,
    val options: List<String>
)
