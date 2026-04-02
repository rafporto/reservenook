package com.reservenook.appointment.infrastructure

import com.reservenook.appointment.domain.AppointmentService
import org.springframework.data.jpa.repository.JpaRepository

interface AppointmentServiceRepository : JpaRepository<AppointmentService, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<AppointmentService>
    fun findByIdAndCompanyId(id: Long, companyId: Long): AppointmentService?
}
