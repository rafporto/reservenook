package com.reservenook.booking.infrastructure

import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingStatus
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository : JpaRepository<Booking, Long> {
    fun findAllByCompanyIdOrderByCreatedAtDesc(companyId: Long): List<Booking>
    fun findAllByCompanyIdAndStatusOrderByCreatedAtDesc(companyId: Long, status: BookingStatus): List<Booking>
    fun findByIdAndCompanyId(id: Long, companyId: Long): Booking?
}
