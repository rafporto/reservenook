package com.reservenook.auth.application

import com.reservenook.auth.domain.PasswordResetToken
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional

class ResetPasswordServiceTest {

    private val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
    private val userAccountRepository = mockk<UserAccountRepository>(relaxed = true)
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)

    private val service = ResetPasswordService(
        passwordResetTokenRepository = passwordResetTokenRepository,
        userAccountRepository = userAccountRepository,
        passwordEncoder = passwordEncoder,
        securityAuditService = securityAuditService
    )

    @Test
    fun `reset updates password and invalidates token`() {
        val user = UserAccount(
            id = 10L,
            email = "admin@acme.com",
            passwordHash = "old-hash",
            status = UserStatus.ACTIVE,
            emailVerified = true,
            passwordVersion = 0
        )
        val token = PasswordResetToken(
            id = 20L,
            token = "valid-token",
            user = user,
            expiresAt = Instant.now().plus(2, ChronoUnit.HOURS)
        )

        every { passwordResetTokenRepository.findByToken("valid-token") } returns token
        every { userAccountRepository.findById(10L) } returns Optional.of(user)
        every { passwordEncoder.encode("NewSecurePass123") } returns "new-hash"
        every { passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(10L) } returns listOf(token)

        val result = service.reset("valid-token", "NewSecurePass123")

        result.redirectTo shouldBe "/en/login"
        user.passwordHash shouldBe "new-hash"
        user.passwordVersion shouldBe 1
        (token.usedAt != null) shouldBe true
        verify(exactly = 1) { passwordEncoder.encode("NewSecurePass123") }
    }

    @Test
    fun `reset rejects missing token`() {
        every { passwordResetTokenRepository.findByToken("missing-token") } returns null

        val exception = org.junit.jupiter.api.assertThrows<ResetPasswordFailedException> {
            service.reset("missing-token", "NewSecurePass123")
        }

        exception.code shouldBe ResetPasswordFailureCode.INVALID_TOKEN
    }

    @Test
    fun `reset rejects expired token`() {
        val user = UserAccount(
            id = 10L,
            email = "admin@acme.com",
            passwordHash = "old-hash",
            status = UserStatus.ACTIVE,
            emailVerified = true,
            passwordVersion = 0
        )
        val token = PasswordResetToken(
            id = 20L,
            token = "expired-token",
            user = user,
            expiresAt = Instant.now().minus(1, ChronoUnit.MINUTES)
        )
        every { passwordResetTokenRepository.findByToken("expired-token") } returns token

        val exception = org.junit.jupiter.api.assertThrows<ResetPasswordFailedException> {
            service.reset("expired-token", "NewSecurePass123")
        }

        exception.code shouldBe ResetPasswordFailureCode.EXPIRED_TOKEN
    }

    @Test
    fun `reset invalidates other active tokens for the same user`() {
        val user = UserAccount(
            id = 10L,
            email = "admin@acme.com",
            passwordHash = "old-hash",
            status = UserStatus.ACTIVE,
            emailVerified = true,
            passwordVersion = 0
        )
        val currentToken = PasswordResetToken(
            id = 20L,
            token = "valid-token",
            user = user,
            expiresAt = Instant.now().plus(2, ChronoUnit.HOURS)
        )
        val olderToken = PasswordResetToken(
            id = 21L,
            token = "older-token",
            user = user,
            expiresAt = Instant.now().plus(1, ChronoUnit.HOURS)
        )

        every { passwordResetTokenRepository.findByToken("valid-token") } returns currentToken
        every { userAccountRepository.findById(10L) } returns Optional.of(user)
        every { passwordEncoder.encode("NewSecurePass123") } returns "new-hash"
        every { passwordResetTokenRepository.findAllByUserIdAndUsedAtIsNull(10L) } returns listOf(currentToken, olderToken)

        service.reset("valid-token", "NewSecurePass123")

        (olderToken.usedAt != null) shouldBe true
    }
}
