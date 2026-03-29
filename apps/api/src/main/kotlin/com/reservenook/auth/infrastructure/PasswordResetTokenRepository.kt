package com.reservenook.auth.infrastructure

import com.reservenook.auth.domain.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findFirstByUserIdOrderByCreatedAtDesc(userId: Long): PasswordResetToken?
    fun findAllByUserIdAndUsedAtIsNull(userId: Long): List<PasswordResetToken>
}
