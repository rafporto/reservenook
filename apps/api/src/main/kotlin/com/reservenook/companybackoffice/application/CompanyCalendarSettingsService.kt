package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeBusinessHourSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClosureDateSummary
import com.reservenook.companybackoffice.domain.CompanyBusinessHour
import com.reservenook.companybackoffice.domain.CompanyClosureDate
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime

@Service
class CompanyCalendarSettingsService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyBusinessHourRepository: CompanyBusinessHourRepository,
    private val companyClosureDateRepository: CompanyClosureDateRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun updateBusinessHours(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<BusinessHourDraft>
    ): List<CompanyBackofficeBusinessHourSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map { entry ->
            val dayOfWeek = CompanyConfigurationSupport.parseDay(entry.dayOfWeek)
            val opensAt = CompanyConfigurationSupport.parseTime(entry.opensAt, "Opening time must use HH:mm format.")
            val closesAt = CompanyConfigurationSupport.parseTime(entry.closesAt, "Closing time must use HH:mm format.")
            if (!opensAt.isBefore(closesAt)) {
                throw IllegalArgumentException("Closing time must be after opening time.")
            }
            NormalizedBusinessHourDraft(dayOfWeek, opensAt, closesAt, entry.displayOrder)
        }

        normalizedEntries.groupBy { it.dayOfWeek }.forEach { (_, dayEntries) ->
            val sorted = dayEntries.sortedBy { it.opensAt }
            sorted.zipWithNext().forEach { (current, next) ->
                if (!current.closesAt.isBefore(next.opensAt)) {
                    throw IllegalArgumentException("Business hour windows cannot overlap on the same day.")
                }
            }
        }

        companyBusinessHourRepository.deleteAllByCompanyId(companyId)
        val saved = companyBusinessHourRepository.saveAll(
            normalizedEntries.map {
                CompanyBusinessHour(
                    company = company,
                    dayOfWeek = it.dayOfWeek,
                    opensAt = it.opensAt,
                    closesAt = it.closesAt,
                    displayOrder = it.displayOrder
                )
            }
        )

        audit(SecurityAuditEventType.COMPANY_BUSINESS_HOURS_UPDATED, principal, company.slug)
        return saved.sortedWith(compareBy<CompanyBusinessHour> { it.dayOfWeek.ordinal }.thenBy { it.displayOrder }).map { it.toSummary() }
    }

    @Transactional
    fun updateClosureDates(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        entries: List<ClosureDateDraft>
    ): List<CompanyBackofficeClosureDateSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val companyId = requireNotNull(company.id)
        val normalizedEntries = entries.map {
            val startsOn = LocalDate.parse(it.startsOn)
            val endsOn = LocalDate.parse(it.endsOn)
            if (endsOn.isBefore(startsOn)) {
                throw IllegalArgumentException("Closure end date must be on or after the start date.")
            }
            NormalizedClosureDateDraft(it.label?.trim()?.ifBlank { null }, startsOn, endsOn)
        }.sortedBy { it.startsOn }

        normalizedEntries.zipWithNext().forEach { (current, next) ->
            if (!current.endsOn.isBefore(next.startsOn)) {
                throw IllegalArgumentException("Closure periods cannot overlap.")
            }
        }

        companyClosureDateRepository.deleteAllByCompanyId(companyId)
        val saved = companyClosureDateRepository.saveAll(
            normalizedEntries.map {
                CompanyClosureDate(
                    company = company,
                    label = it.label,
                    startsOn = it.startsOn,
                    endsOn = it.endsOn
                )
            }
        )

        audit(SecurityAuditEventType.COMPANY_CLOSURE_DATES_UPDATED, principal, company.slug)
        return saved.map { it.toSummary() }
    }

    private fun audit(eventType: SecurityAuditEventType, principal: AppAuthenticatedUser, companySlug: String) {
        securityAuditService.record(
            eventType = eventType,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = companySlug
        )
    }
}

private data class NormalizedBusinessHourDraft(
    val dayOfWeek: com.reservenook.companybackoffice.domain.BusinessDay,
    val opensAt: LocalTime,
    val closesAt: LocalTime,
    val displayOrder: Int
)

private data class NormalizedClosureDateDraft(
    val label: String?,
    val startsOn: LocalDate,
    val endsOn: LocalDate
)
