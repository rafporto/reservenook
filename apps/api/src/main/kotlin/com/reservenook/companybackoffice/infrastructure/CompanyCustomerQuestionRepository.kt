package com.reservenook.companybackoffice.infrastructure

import com.reservenook.companybackoffice.domain.CompanyCustomerQuestion
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyCustomerQuestionRepository : JpaRepository<CompanyCustomerQuestion, Long> {
    fun findAllByCompanyIdOrderByDisplayOrderAsc(companyId: Long): List<CompanyCustomerQuestion>
    fun deleteAllByCompanyId(companyId: Long)
}
