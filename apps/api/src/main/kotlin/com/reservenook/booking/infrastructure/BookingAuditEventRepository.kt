package com.reservenook.booking.infrastructure

import com.reservenook.booking.domain.BookingAuditEvent
import org.springframework.data.jpa.repository.JpaRepository

interface BookingAuditEventRepository : JpaRepository<BookingAuditEvent, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<BookingAuditEvent>
}
