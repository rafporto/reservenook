package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@Service
class PlatformRetentionService(
    private val companyRepository: CompanyRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun updateLegalHold(principal: AppAuthenticatedUser, companySlug: String, legalHoldUntil: Instant?): Instant? {
        if (!principal.isPlatformAdmin) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.")
        }
        if (legalHoldUntil != null && legalHoldUntil.isBefore(Instant.now().minusSeconds(60))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Legal hold must be in the future when set.")
        }
        val company = companyRepository.findBySlug(companySlug)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found.")
        company.legalHoldUntil = legalHoldUntil
        companyRepository.save(company)
        securityAuditService.record(
            eventType = SecurityAuditEventType.COMPANY_LEGAL_HOLD_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            details = legalHoldUntil?.toString() ?: "cleared"
        )
        return company.legalHoldUntil
    }
}
