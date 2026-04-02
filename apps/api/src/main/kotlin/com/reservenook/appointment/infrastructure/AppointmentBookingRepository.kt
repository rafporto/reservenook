package com.reservenook.appointment.infrastructure

import com.reservenook.appointment.domain.AppointmentBooking
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface AppointmentBookingRepository : JpaRepository<AppointmentBooking, Long> {
    fun findAllByCompanyIdOrderByStartsAtAsc(companyId: Long): List<AppointmentBooking>
    fun findAllByCompanyIdAndStartsAtBetweenOrderByStartsAtAsc(companyId: Long, startsAt: Instant, endsAt: Instant): List<AppointmentBooking>
    fun findAllByProviderIdAndStartsAtBetweenOrderByStartsAtAsc(providerId: Long, startsAt: Instant, endsAt: Instant): List<AppointmentBooking>
    fun findAllByCompanyIdAndStartsAtBetweenAndBookingStatusInOrderByStartsAtAsc(
        companyId: Long,
        startsAt: Instant,
        endsAt: Instant,
        statuses: Collection<com.reservenook.booking.domain.BookingStatus>
    ): List<AppointmentBooking>
    fun findFirstByBookingId(bookingId: Long): AppointmentBooking?
}
