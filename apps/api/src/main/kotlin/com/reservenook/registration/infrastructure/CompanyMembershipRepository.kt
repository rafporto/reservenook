package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyMembershipRepository : JpaRepository<CompanyMembership, Long>
{
    fun findFirstByUserId(userId: Long): CompanyMembership?
    fun findFirstByUserIdAndCompanySlug(userId: Long, companySlug: String): CompanyMembership?
    fun findFirstByUserEmailAndCompanySlug(userEmail: String, companySlug: String): CompanyMembership?
    fun findByIdAndCompanyId(id: Long, companyId: Long): CompanyMembership?
    fun findAllByCompanyId(companyId: Long): List<CompanyMembership>
    fun findAllByCompanyIdAndRole(companyId: Long, role: CompanyRole): List<CompanyMembership>
    fun countByCompanyIdAndRole(companyId: Long, role: CompanyRole): Long
    fun countByCompanyIdAndRoleAndUserStatus(companyId: Long, role: CompanyRole, userStatus: UserStatus): Long
    fun countByUserId(userId: Long): Long
    fun deleteAllByCompanyId(companyId: Long)
}
