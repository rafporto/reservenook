package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.domain.InactivityNotificationEvent
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import org.springframework.data.jpa.repository.JpaRepository

interface InactivityNotificationEventRepository : JpaRepository<InactivityNotificationEvent, Long> {
    fun existsByCompanyIdAndStatus(companyId: Long, status: InactivityNotificationStatus): Boolean
    fun findAllByCompanyId(companyId: Long): List<InactivityNotificationEvent>
}
