package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeStaffUserSummary
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import com.reservenook.shared.validation.CommonInputValidation
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CompanyStaffManagementService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val userAccountRepository: UserAccountRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordResetMailSender: PasswordResetMailSender,
    private val passwordEncoder: PasswordEncoder,
    private val registrationProperties: RegistrationProperties,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun list(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeStaffUserSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return companyMembershipRepository.findAllByCompanyId(requireNotNull(membership.company.id))
            .sortedBy { it.createdAt }
            .map { it.toStaffSummary() }
    }

    @Transactional
    fun create(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        role: String
    ): CompanyBackofficeStaffUserSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company
        val normalizedEmail = CommonInputValidation.requireEmail(email, "Staff email must be a valid email address.")
        val normalizedRole = CompanyConfigurationSupport.parseRole(role)

        if (fullName.trim().isBlank()) {
            throw IllegalArgumentException("Staff full name is required.")
        }
        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw IllegalArgumentException("Email address is already in use.")
        }

        val user = userAccountRepository.save(
            UserAccount(
                email = normalizedEmail,
                fullName = fullName.trim(),
                passwordHash = passwordEncoder.encode(UUID.randomUUID().toString()),
                status = UserStatus.ACTIVE,
                emailVerified = true
            )
        )

        val savedMembership = companyMembershipRepository.save(
            CompanyMembership(
                company = company,
                user = user,
                role = normalizedRole
            )
        )

        val resetToken = passwordResetTokenRepository.save(
            PasswordResetToken(
                token = UUID.randomUUID().toString(),
                user = user,
                expiresAt = Instant.now().plus(registrationProperties.passwordResetTokenHours, ChronoUnit.HOURS)
            )
        )

        passwordResetMailSender.sendPasswordResetEmail(
            normalizedEmail,
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/${company.defaultLanguage}/reset-password?token=${resetToken.token}",
            company.defaultLanguage
        )

        audit(SecurityAuditEventType.COMPANY_STAFF_USER_CREATED, principal, company.slug, normalizedEmail)
        return savedMembership.toStaffSummary()
    }

    @Transactional
    fun update(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        membershipId: Long,
        role: String,
        status: String
    ): CompanyBackofficeStaffUserSummary {
        val actingMembership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = actingMembership.company
        val companyId = requireNotNull(company.id)
        val targetMembership = companyMembershipRepository.findByIdAndCompanyId(membershipId, companyId)
            ?: throw IllegalArgumentException("Staff user could not be found.")

        val nextRole = CompanyConfigurationSupport.parseRole(role)
        val nextStatus = CompanyConfigurationSupport.parseUserStatus(status)

        if (targetMembership.user.id == principal.userId && (nextRole != CompanyRole.COMPANY_ADMIN || nextStatus != UserStatus.ACTIVE)) {
            throw IllegalArgumentException("You cannot remove your own active company-admin access.")
        }

        val isCurrentlyAdmin = targetMembership.role == CompanyRole.COMPANY_ADMIN
        val isLosingAdminCoverage = isCurrentlyAdmin && nextRole != CompanyRole.COMPANY_ADMIN
        val isDisablingAdmin = isCurrentlyAdmin && nextStatus != UserStatus.ACTIVE
        val activeAdminCount = companyMembershipRepository.countByCompanyIdAndRoleAndUserStatus(
            companyId,
            CompanyRole.COMPANY_ADMIN,
            UserStatus.ACTIVE
        )
        if ((isLosingAdminCoverage || isDisablingAdmin) && activeAdminCount <= 1) {
            throw IllegalArgumentException("At least one company admin must remain active.")
        }

        targetMembership.role = nextRole
        targetMembership.user.status = nextStatus

        audit(SecurityAuditEventType.COMPANY_STAFF_USER_UPDATED, principal, company.slug, targetMembership.user.email)
        return targetMembership.toStaffSummary()
    }

    private fun audit(eventType: SecurityAuditEventType, principal: AppAuthenticatedUser, companySlug: String, targetEmail: String) {
        securityAuditService.record(
            eventType = eventType,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = companySlug,
            targetEmail = targetEmail
        )
    }
}
