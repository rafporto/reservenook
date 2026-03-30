package com.reservenook.auth.application

import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ResetPasswordService(
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun reset(token: String, password: String): ResetPasswordResult {
        val now = Instant.now()
        val resetToken = passwordResetTokenRepository.findByToken(token.trim())
            ?: throw ResetPasswordFailedException(
                "This password reset link is invalid. Request a new password reset email.",
                ResetPasswordFailureCode.INVALID_TOKEN
            )

        if (resetToken.usedAt != null || resetToken.expiresAt.isBefore(now)) {
            throw ResetPasswordFailedException(
                "This password reset link has expired. Request a new password reset email.",
                ResetPasswordFailureCode.EXPIRED_TOKEN
            )
        }

        val user = userAccountRepository.findById(requireNotNull(resetToken.user.id))
            .orElseThrow {
                ResetPasswordFailedException(
                    "This password reset link is invalid. Request a new password reset email.",
                    ResetPasswordFailureCode.INVALID_TOKEN
                )
            }

        user.passwordHash = passwordEncoder.encode(password)
        resetToken.usedAt = now

        passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(requireNotNull(user.id))
            .filter { it.id != resetToken.id }
            .forEach { existingToken ->
                existingToken.usedAt = now
            }

        return ResetPasswordResult(
            message = "Your password has been updated. Continue to login.",
            redirectTo = "/en/login"
        )
    }
}
