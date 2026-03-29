package com.reservenook.registration.application

import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class CompanyActivationService(
    private val activationTokenRepository: ActivationTokenRepository
) {

    @Transactional
    fun activate(tokenValue: String): ActivateCompanyResult {
        val token = activationTokenRepository.findByToken(tokenValue.trim())
            ?: return ActivateCompanyResult(
                outcome = ActivationOutcome.INVALID,
                message = "The activation link is invalid."
            )

        val now = Instant.now()
        val company = token.company
        val user = token.user

        if (token.usedAt != null || (
                company.status == CompanyStatus.ACTIVE &&
                    user.status == UserStatus.ACTIVE &&
                    user.emailVerified
                )
        ) {
            if (token.usedAt == null) {
                token.usedAt = now
            }

            return ActivateCompanyResult(
                outcome = ActivationOutcome.ALREADY_ACTIVE,
                message = "The company account is already active."
            )
        }

        if (token.expiresAt.isBefore(now)) {
            return ActivateCompanyResult(
                outcome = ActivationOutcome.EXPIRED,
                message = "The activation link has expired."
            )
        }

        company.status = CompanyStatus.ACTIVE
        user.status = UserStatus.ACTIVE
        user.emailVerified = true
        token.usedAt = now

        return ActivateCompanyResult(
            outcome = ActivationOutcome.ACTIVATED,
            message = "The company account has been activated."
        )
    }
}
