package com.reservenook.widget.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.widget.domain.WidgetUsageEvent
import com.reservenook.widget.domain.WidgetUsageEventType
import com.reservenook.widget.infrastructure.WidgetUsageEventRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

data class WidgetUsageOriginSummary(
    val originHost: String,
    val bootstrapCount: Int,
    val bookingCount: Int
)

data class WidgetUsageSummary(
    val bootstrapsLast7Days: Int,
    val bookingsLast7Days: Int,
    val recentOrigins: List<WidgetUsageOriginSummary>
)

@Service
class WidgetUsageService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val widgetUsageEventRepository: WidgetUsageEventRepository
) {
    fun record(company: com.reservenook.registration.domain.Company, originHost: String, eventType: WidgetUsageEventType) {
        widgetUsageEventRepository.save(
            WidgetUsageEvent(
                company = company,
                originHost = originHost,
                eventType = eventType
            )
        )
    }

    fun getSummary(principal: AppAuthenticatedUser, requestedSlug: String): WidgetUsageSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val companyId = requireNotNull(membership.company.id)
        return buildSummary(companyId)
    }

    fun buildSummary(companyId: Long): WidgetUsageSummary {
        val since = Instant.now().minus(7, ChronoUnit.DAYS)
        val events = widgetUsageEventRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId)
        val grouped = events.groupBy { it.originHost }
        return WidgetUsageSummary(
            bootstrapsLast7Days = widgetUsageEventRepository.countByCompanyIdAndEventTypeAndCreatedAtAfter(companyId, WidgetUsageEventType.BOOTSTRAP_INITIALIZED, since).toInt(),
            bookingsLast7Days = widgetUsageEventRepository.countByCompanyIdAndEventTypeAndCreatedAtAfter(companyId, WidgetUsageEventType.BOOKING_FLOW_COMPLETED, since).toInt(),
            recentOrigins = grouped.entries.take(5).map { entry ->
                WidgetUsageOriginSummary(
                    originHost = entry.key,
                    bootstrapCount = entry.value.count { it.eventType == WidgetUsageEventType.BOOTSTRAP_INITIALIZED },
                    bookingCount = entry.value.count { it.eventType == WidgetUsageEventType.BOOKING_FLOW_COMPLETED }
                )
            }
        )
    }
}
