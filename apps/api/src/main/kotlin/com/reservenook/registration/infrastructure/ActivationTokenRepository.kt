package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.ActivationToken
import org.springframework.data.jpa.repository.JpaRepository

interface ActivationTokenRepository : JpaRepository<ActivationToken, Long> {
    fun findByToken(token: String): ActivationToken?
}
