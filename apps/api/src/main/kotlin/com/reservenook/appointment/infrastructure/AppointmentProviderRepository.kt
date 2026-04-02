package com.reservenook.appointment.infrastructure

import com.reservenook.appointment.domain.AppointmentProvider
import org.springframework.data.jpa.repository.JpaRepository

interface AppointmentProviderRepository : JpaRepository<AppointmentProvider, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<AppointmentProvider>
    fun findByIdAndCompanyId(id: Long, companyId: Long): AppointmentProvider?
    fun findFirstByCompanyIdAndLinkedUserId(companyId: Long, linkedUserId: Long): AppointmentProvider?
}
