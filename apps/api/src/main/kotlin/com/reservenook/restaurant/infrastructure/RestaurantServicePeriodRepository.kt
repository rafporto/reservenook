package com.reservenook.restaurant.infrastructure

import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.restaurant.domain.RestaurantServicePeriod
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantServicePeriodRepository : JpaRepository<RestaurantServicePeriod, Long> {
    fun findAllByCompanyIdOrderByDayOfWeekAscOpensAtAsc(companyId: Long): List<RestaurantServicePeriod>
    fun findAllByCompanyIdAndDayOfWeekAndActiveTrueOrderByOpensAtAsc(companyId: Long, dayOfWeek: BusinessDay): List<RestaurantServicePeriod>
    fun findByIdAndCompanyId(id: Long, companyId: Long): RestaurantServicePeriod?
}
