package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.domain.CompanyDeletionEvent
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyDeletionEventRepository : JpaRepository<CompanyDeletionEvent, Long> {
    fun findAllByCompanyId(companyId: Long): List<CompanyDeletionEvent>
}
