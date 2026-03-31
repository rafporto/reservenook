package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.CompanySubscription
import org.springframework.data.jpa.repository.JpaRepository

interface CompanySubscriptionRepository : JpaRepository<CompanySubscription, Long> {
    fun findFirstByCompanyIdOrderByExpiresAtDesc(companyId: Long): CompanySubscription?
    fun deleteAllByCompanyId(companyId: Long)
}
