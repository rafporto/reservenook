package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.domain.InactivityNotificationEvent
import com.reservenook.companylifecycle.domain.CompanyLifecycleNotificationType
import com.reservenook.companylifecycle.domain.InactivityNotificationStatus
import org.springframework.data.jpa.repository.JpaRepository

interface InactivityNotificationEventRepository : JpaRepository<InactivityNotificationEvent, Long> {
    fun existsByCompanyIdAndNotificationTypeAndStatus(
        companyId: Long,
        notificationType: CompanyLifecycleNotificationType,
        status: InactivityNotificationStatus
    ): Boolean
    fun findAllByCompanyId(companyId: Long): List<InactivityNotificationEvent>
    fun deleteAllByCompanyId(companyId: Long)
}
