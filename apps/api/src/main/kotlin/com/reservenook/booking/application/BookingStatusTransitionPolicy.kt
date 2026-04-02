package com.reservenook.booking.application

import com.reservenook.booking.domain.BookingStatus

object BookingStatusTransitionPolicy {

    fun requireAllowed(currentStatus: BookingStatus, nextStatus: BookingStatus) {
        if (currentStatus == nextStatus) {
            throw IllegalArgumentException("Booking is already in the requested status.")
        }
        if (currentStatus == BookingStatus.CANCELLED && nextStatus != BookingStatus.CANCELLED) {
            throw IllegalArgumentException("Cancelled bookings cannot be reopened.")
        }
        if (currentStatus == BookingStatus.COMPLETED && nextStatus != BookingStatus.COMPLETED) {
            throw IllegalArgumentException("Completed bookings cannot change status.")
        }
    }
}
