package com.reservenook.groupclass.infrastructure

import com.reservenook.groupclass.domain.ClassBooking
import com.reservenook.groupclass.domain.ClassBookingStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ClassBookingRepository : JpaRepository<ClassBooking, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<ClassBooking>
    fun findAllByClassSessionIdOrderByCreatedAtAsc(classSessionId: Long): List<ClassBooking>
    fun countByClassSessionIdAndStatusIn(classSessionId: Long, statuses: Collection<ClassBookingStatus>): Long
    fun findFirstByClassSessionIdAndBookingCustomerContactIdAndStatusIn(
        classSessionId: Long,
        customerContactId: Long,
        statuses: Collection<ClassBookingStatus>
    ): ClassBooking?
    fun findFirstByClassSessionIdAndStatusOrderByWaitlistPositionAsc(classSessionId: Long, status: ClassBookingStatus): ClassBooking?
    fun findByBookingIdAndCompanyId(bookingId: Long, companyId: Long): ClassBooking?
}
