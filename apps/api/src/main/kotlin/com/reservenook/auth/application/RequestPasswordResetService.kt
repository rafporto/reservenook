package com.reservenook.auth.application

import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.security.application.RequestThrottleService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class RequestPasswordResetService(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordResetMailSender: PasswordResetMailSender,
    private val registrationProperties: RegistrationProperties,
    private val requestThrottleService: RequestThrottleService
) {

    @Transactional
    fun request(email: String, requestFingerprint: String): RequestPasswordResetResult {
        val normalizedEmail = email.trim().lowercase()
        val neutralResult = RequestPasswordResetResult(
            message = "If the account is eligible, a password reset email will be sent."
        )
        requestThrottleService.assertAllowed("forgot-password", requestFingerprint, 5, Duration.ofMinutes(10))

        val user = userAccountRepository.findByEmail(normalizedEmail) ?: return neutralResult
        if (user.status != UserStatus.ACTIVE || !user.emailVerified) {
            return neutralResult
        }

        val now = Instant.now()
        val lastToken = passwordResetTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(requireNotNull(user.id))
        if (lastToken != null && lastToken.createdAt.isAfter(now.minus(registrationProperties.passwordResetCooldownMinutes, ChronoUnit.MINUTES))) {
            return neutralResult
        }

        passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(requireNotNull(user.id)).forEach { token ->
            token.usedAt = now
        }

        val nextToken = passwordResetTokenRepository.save(
            PasswordResetToken(
                token = UUID.randomUUID().toString(),
                user = user,
                expiresAt = now.plus(registrationProperties.passwordResetTokenHours, ChronoUnit.HOURS)
            )
        )

        val language = companyMembershipRepository.findFirstByUserId(requireNotNull(user.id))
            ?.company
            ?.defaultLanguage
            ?.lowercase()
            ?: "en"

        passwordResetMailSender.sendPasswordResetEmail(
            normalizedEmail,
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/$language/reset-password?token=${nextToken.token}",
            language
        )

        return neutralResult
    }
}
