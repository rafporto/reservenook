package com.reservenook.companylifecycle.application

import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companylifecycle.domain.CompanyDeletionEvent
import com.reservenook.companylifecycle.domain.CompanyDeletionEventStatus
import com.reservenook.companylifecycle.infrastructure.CompanyDeletionEventRepository
import com.reservenook.companylifecycle.infrastructure.InactivityNotificationEventRepository
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CompanyDeletionService(
    private val companyRepository: CompanyRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val companySubscriptionRepository: CompanySubscriptionRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val inactivityNotificationEventRepository: InactivityNotificationEventRepository,
    private val companyDeletionEventRepository: CompanyDeletionEventRepository,
    private val userAccountRepository: UserAccountRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun deletePendingCompanies(now: Instant = Instant.now()): CompanyDeletionResult {
        var deletedCompanies = 0
        var failedDeletions = 0

        val companiesToDelete = companyRepository.findAllByStatus(CompanyStatus.PENDING_DELETION)
            .filter { company ->
                val deletionScheduledAt = company.deletionScheduledAt
                val legalHoldUntil = company.legalHoldUntil
                deletionScheduledAt != null &&
                    !deletionScheduledAt.isAfter(now) &&
                    (legalHoldUntil == null || legalHoldUntil.isBefore(now))
            }

        companiesToDelete.forEach { company ->
            val companyId = requireNotNull(company.id)
            val companyName = company.name
            val companySlug = company.slug

            try {
                val memberships = companyMembershipRepository.findAllByCompanyId(companyId)
                val userIds = memberships.mapNotNull { membership -> membership.user.id }.distinct()

                companyMembershipRepository.deleteAllByCompanyId(companyId)
                companySubscriptionRepository.deleteAllByCompanyId(companyId)
                activationTokenRepository.deleteAllByCompanyId(companyId)
                inactivityNotificationEventRepository.deleteAllByCompanyId(companyId)
                companyRepository.delete(company)

                userIds.forEach { userId ->
                    if (companyMembershipRepository.countByUserId(userId) == 0L) {
                        activationTokenRepository.deleteAllByUserId(userId)
                        passwordResetTokenRepository.deleteAllByUserId(userId)
                        userAccountRepository.findById(userId).ifPresent { user ->
                            if (!user.isPlatformAdmin) {
                                userAccountRepository.delete(user)
                            }
                        }
                    }
                }

                companyDeletionEventRepository.save(
                    CompanyDeletionEvent(
                        companyId = companyId,
                        companyName = companyName,
                        companySlug = companySlug,
                        status = CompanyDeletionEventStatus.SUCCEEDED,
                        deletedAt = now
                    )
                )
                securityAuditService.record(
                    eventType = SecurityAuditEventType.COMPANY_DELETED,
                    outcome = SecurityAuditOutcome.SUCCESS,
                    companySlug = companySlug
                )
                deletedCompanies += 1
            } catch (exception: Exception) {
                companyDeletionEventRepository.save(
                    CompanyDeletionEvent(
                        companyId = companyId,
                        companyName = companyName,
                        companySlug = companySlug,
                        status = CompanyDeletionEventStatus.FAILED,
                        failureReason = exception.message
                    )
                )
                securityAuditService.record(
                    eventType = SecurityAuditEventType.COMPANY_DELETION_FAILED,
                    outcome = SecurityAuditOutcome.FAILURE,
                    companySlug = companySlug,
                    details = exception.message
                )
                failedDeletions += 1
            }
        }

        return CompanyDeletionResult(
            deletedCompanies = deletedCompanies,
            failedDeletions = failedDeletions
        )
    }
}
