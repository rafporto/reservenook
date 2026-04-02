package com.reservenook.companybackoffice.infrastructure

import com.reservenook.companybackoffice.domain.CompanyBusinessHour
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyBusinessHourRepository : JpaRepository<CompanyBusinessHour, Long> {
    fun findAllByCompanyIdOrderByDayOfWeekAscDisplayOrderAsc(companyId: Long): List<CompanyBusinessHour>
    fun findAllByCompanyIdAndDayOfWeekOrderByDisplayOrderAsc(companyId: Long, dayOfWeek: com.reservenook.companybackoffice.domain.BusinessDay): List<CompanyBusinessHour>
    fun deleteAllByCompanyId(companyId: Long)
}
