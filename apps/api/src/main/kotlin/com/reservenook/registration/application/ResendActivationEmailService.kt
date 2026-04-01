package com.reservenook.registration.application

import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.PublicRequestAbuseGuard
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.application.TooManyRequestsException
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ResendActivationEmailService(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val registrationMailSender: RegistrationMailSender,
    private val registrationProperties: RegistrationProperties,
    private val publicRequestAbuseGuard: PublicRequestAbuseGuard,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun resend(email: String, requestFingerprint: String): ResendActivationEmailResult {
        val normalizedEmail = email.trim().lowercase()
        val neutralResult = ResendActivationEmailResult(
            message = "If the account is pending activation, a new activation email will be sent."
        )
        try {
            val clientAddress = requestFingerprint.substringBefore("|")
            publicRequestAbuseGuard.assertAllowed("resend-activation", clientAddress, normalizedEmail)
        } catch (exception: TooManyRequestsException) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.ACTIVATION_RESEND_RATE_LIMITED,
                outcome = SecurityAuditOutcome.RATE_LIMITED,
                targetEmail = normalizedEmail
            )
            throw exception
        }

        val user = userAccountRepository.findByEmail(normalizedEmail)
        if (user == null) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.ACTIVATION_RESEND_REQUESTED,
                outcome = SecurityAuditOutcome.NEUTRAL,
                targetEmail = normalizedEmail
            )
            return neutralResult
        }
        if (user.status == UserStatus.ACTIVE || user.emailVerified) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.ACTIVATION_RESEND_REQUESTED,
                outcome = SecurityAuditOutcome.NEUTRAL,
                actorUserId = user.id,
                actorEmail = user.email,
                targetEmail = normalizedEmail
            )
            return neutralResult
        }

        val membership = companyMembershipRepository.findFirstByUserId(requireNotNull(user.id))
        if (membership == null) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.ACTIVATION_RESEND_REQUESTED,
                outcome = SecurityAuditOutcome.NEUTRAL,
                actorUserId = user.id,
                actorEmail = user.email,
                targetEmail = normalizedEmail
            )
            return neutralResult
        }
        val company = membership.company

        if (company.status != CompanyStatus.PENDING_ACTIVATION) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.ACTIVATION_RESEND_REQUESTED,
                outcome = SecurityAuditOutcome.NEUTRAL,
                actorUserId = user.id,
                actorEmail = user.email,
                companySlug = company.slug,
                targetEmail = normalizedEmail
            )
            return neutralResult
        }

        val now = Instant.now()
        val lastToken = activationTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(requireNotNull(user.id))
        if (lastToken != null && lastToken.createdAt.isAfter(now.minus(registrationProperties.resendCooldownMinutes, ChronoUnit.MINUTES))) {
            return neutralResult
        }

        activationTokenRepository.findAllByUserIdAndUsedAtIsNull(requireNotNull(user.id)).forEach { token ->
            token.usedAt = now
        }

        val nextToken = activationTokenRepository.save(
            ActivationToken(
                token = UUID.randomUUID().toString(),
                company = company,
                user = user,
                expiresAt = now.plus(registrationProperties.activationTokenHours, ChronoUnit.HOURS)
            )
        )

        registrationMailSender.sendActivationEmail(
            normalizedEmail,
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/${company.defaultLanguage}/activate?token=${nextToken.token}",
            company.defaultLanguage
        )

        securityAuditService.record(
            eventType = SecurityAuditEventType.ACTIVATION_RESEND_REQUESTED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = user.id,
            actorEmail = user.email,
            companySlug = company.slug,
            targetEmail = normalizedEmail
        )

        return neutralResult
    }
}
