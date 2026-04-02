package com.reservenook.appointment.application

import com.reservenook.companybackoffice.domain.BusinessDay
import java.time.LocalTime

object AppointmentSupport {

    fun parseDay(value: String): BusinessDay = try {
        BusinessDay.valueOf(value.trim().uppercase())
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Day of week is not supported.")
    }

    fun parseTime(value: String, message: String): LocalTime = try {
        LocalTime.parse(value)
    } catch (_: Exception) {
        throw IllegalArgumentException(message)
    }
}
