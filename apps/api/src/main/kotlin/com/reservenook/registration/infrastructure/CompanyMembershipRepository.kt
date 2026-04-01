package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.CompanyMembership
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyMembershipRepository : JpaRepository<CompanyMembership, Long>
{
    fun findFirstByUserId(userId: Long): CompanyMembership?
    fun findFirstByUserIdAndCompanySlug(userId: Long, companySlug: String): CompanyMembership?
    fun findFirstByUserEmailAndCompanySlug(userEmail: String, companySlug: String): CompanyMembership?
    fun findByIdAndCompanyId(id: Long, companyId: Long): CompanyMembership?
    fun findAllByCompanyId(companyId: Long): List<CompanyMembership>
    fun findAllByCompanyIdAndRole(companyId: Long, role: com.reservenook.registration.domain.CompanyRole): List<CompanyMembership>
    fun countByCompanyIdAndRole(companyId: Long, role: com.reservenook.registration.domain.CompanyRole): Long
    fun countByUserId(userId: Long): Long
    fun deleteAllByCompanyId(companyId: Long)
}
