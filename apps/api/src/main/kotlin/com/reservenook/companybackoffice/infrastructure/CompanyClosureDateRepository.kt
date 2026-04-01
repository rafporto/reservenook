package com.reservenook.companybackoffice.infrastructure

import com.reservenook.companybackoffice.domain.CompanyClosureDate
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyClosureDateRepository : JpaRepository<CompanyClosureDate, Long> {
    fun findAllByCompanyIdOrderByStartsOnAsc(companyId: Long): List<CompanyClosureDate>
    fun deleteAllByCompanyId(companyId: Long)
}
