package com.reservenook.restaurant.infrastructure

import com.reservenook.restaurant.domain.RestaurantTable
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface RestaurantTableRepository : JpaRepository<RestaurantTable, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<RestaurantTable>
    fun findByIdAndCompanyId(id: Long, companyId: Long): RestaurantTable?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RestaurantTable t where t.company.id = :companyId and t.active = true")
    fun findAllActiveByCompanyIdForUpdate(companyId: Long): List<RestaurantTable>
}
