package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.UserAccount
import org.springframework.data.jpa.repository.JpaRepository

interface UserAccountRepository : JpaRepository<UserAccount, Long> {
    fun existsByEmail(email: String): Boolean
}
