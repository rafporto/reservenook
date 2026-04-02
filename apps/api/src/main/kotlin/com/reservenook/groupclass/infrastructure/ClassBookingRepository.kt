package com.reservenook.groupclass.infrastructure

import com.reservenook.groupclass.domain.ClassBooking
import com.reservenook.groupclass.domain.ClassBookingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ClassSessionBookingCount {
    val classSessionId: Long
    val status: ClassBookingStatus
    val total: Long
}

interface ClassBookingRepository : JpaRepository<ClassBooking, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<ClassBooking>
    fun findAllByClassSessionIdOrderByCreatedAtAsc(classSessionId: Long): List<ClassBooking>
    fun countByClassSessionIdAndStatusIn(classSessionId: Long, statuses: Collection<ClassBookingStatus>): Long
    @Query(
        """
        select cb.classSession.id as classSessionId, cb.status as status, count(cb) as total
        from ClassBooking cb
        where cb.classSession.id in :classSessionIds
        group by cb.classSession.id, cb.status
        """
    )
    fun summarizeByClassSessionIds(classSessionIds: Collection<Long>): List<ClassSessionBookingCount>
    fun findFirstByClassSessionIdAndBookingCustomerContactIdAndStatusIn(
        classSessionId: Long,
        customerContactId: Long,
        statuses: Collection<ClassBookingStatus>
    ): ClassBooking?
    fun findFirstByClassSessionIdAndStatusOrderByWaitlistPositionAsc(classSessionId: Long, status: ClassBookingStatus): ClassBooking?
    fun findByBookingIdAndCompanyId(bookingId: Long, companyId: Long): ClassBooking?
}
