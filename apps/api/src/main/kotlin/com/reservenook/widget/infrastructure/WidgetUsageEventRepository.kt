package com.reservenook.widget.infrastructure

import com.reservenook.widget.domain.WidgetUsageEvent
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WidgetUsageOriginAggregate {
    val originHost: String
    val bootstrapCount: Long
    val bookingCount: Long
}

interface WidgetUsageEventRepository : JpaRepository<WidgetUsageEvent, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<WidgetUsageEvent>
    fun countByCompanyIdAndEventTypeAndCreatedAtAfter(companyId: Long, eventType: com.reservenook.widget.domain.WidgetUsageEventType, createdAt: Instant): Long
    @Query(
        """
        select e.originHost as originHost,
               sum(case when e.eventType = com.reservenook.widget.domain.WidgetUsageEventType.BOOTSTRAP_INITIALIZED then 1 else 0 end) as bootstrapCount,
               sum(case when e.eventType = com.reservenook.widget.domain.WidgetUsageEventType.BOOKING_FLOW_COMPLETED then 1 else 0 end) as bookingCount
        from WidgetUsageEvent e
        where e.company.id = :companyId
        group by e.originHost
        order by max(e.createdAt) desc
        """
    )
    fun summarizeOriginsByCompanyId(companyId: Long): List<WidgetUsageOriginAggregate>
}
