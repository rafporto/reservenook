package com.reservenook.restaurant.infrastructure

import com.reservenook.restaurant.domain.RestaurantTableCombination
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantTableCombinationRepository : JpaRepository<RestaurantTableCombination, Long> {
    fun findAllByCompanyIdOrderByCreatedAtAsc(companyId: Long): List<RestaurantTableCombination>
    fun deleteAllByCompanyId(companyId: Long)
}
