package com.reservenook.restaurant.infrastructure

import com.reservenook.restaurant.domain.DiningArea
import org.springframework.data.jpa.repository.JpaRepository

interface DiningAreaRepository : JpaRepository<DiningArea, Long> {
    fun findAllByCompanyIdOrderByDisplayOrderAscCreatedAtAsc(companyId: Long): List<DiningArea>
    fun findByIdAndCompanyId(id: Long, companyId: Long): DiningArea?
}
