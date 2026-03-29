package com.reservenook.registration.application

import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ResendActivationEmailService(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val registrationMailSender: RegistrationMailSender,
    private val registrationProperties: RegistrationProperties
) {

    @Transactional
    fun resend(email: String): ResendActivationEmailResult {
        val normalizedEmail = email.trim().lowercase()
        val neutralResult = ResendActivationEmailResult(
            message = "If the account is pending activation, a new activation email will be sent."
        )

        val user = userAccountRepository.findByEmail(normalizedEmail) ?: return neutralResult
        if (user.status == UserStatus.ACTIVE || user.emailVerified) {
            return neutralResult
        }

        val membership = companyMembershipRepository.findFirstByUserId(requireNotNull(user.id)) ?: return neutralResult
        val company = membership.company

        if (company.status != CompanyStatus.PENDING_ACTIVATION) {
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
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/${company.defaultLanguage}/activate?token=${nextToken.token}"
        )

        return neutralResult
    }
}
