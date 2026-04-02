package com.reservenook.appointment.infrastructure

import com.reservenook.appointment.domain.AppointmentProviderAvailability
import org.springframework.data.jpa.repository.JpaRepository

interface AppointmentProviderAvailabilityRepository : JpaRepository<AppointmentProviderAvailability, Long> {
    fun findAllByProviderIdOrderByDayOfWeekAscDisplayOrderAsc(providerId: Long): List<AppointmentProviderAvailability>
    fun findAllByProviderIdInOrderByProviderIdAscDayOfWeekAscDisplayOrderAsc(providerIds: Collection<Long>): List<AppointmentProviderAvailability>
    fun deleteAllByProviderId(providerId: Long)
}
