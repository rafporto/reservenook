package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.CompanyMembership
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyMembershipRepository : JpaRepository<CompanyMembership, Long>
{
    fun findFirstByUserId(userId: Long): CompanyMembership?
    fun findFirstByUserIdAndCompanySlug(userId: Long, companySlug: String): CompanyMembership?
}
