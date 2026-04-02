package com.reservenook.widget.infrastructure

import com.reservenook.widget.domain.WidgetUsageEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface WidgetUsageEventRepository : JpaRepository<WidgetUsageEvent, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<WidgetUsageEvent>
    fun countByCompanyIdAndEventTypeAndCreatedAtAfter(companyId: Long, eventType: com.reservenook.widget.domain.WidgetUsageEventType, createdAt: Instant): Long
}
