package com.reservenook.booking.infrastructure

import com.reservenook.booking.domain.CustomerContact
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerContactRepository : JpaRepository<CustomerContact, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<CustomerContact>
    fun findByIdAndCompanyId(id: Long, companyId: Long): CustomerContact?
    fun findFirstByCompanyIdAndNormalizedEmail(companyId: Long, normalizedEmail: String): CustomerContact?
}
