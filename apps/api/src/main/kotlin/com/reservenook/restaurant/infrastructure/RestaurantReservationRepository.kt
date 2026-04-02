package com.reservenook.restaurant.infrastructure

import com.reservenook.restaurant.domain.RestaurantReservation
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface RestaurantReservationRepository : JpaRepository<RestaurantReservation, Long> {
    fun findAllByCompanyIdOrderByReservedAtAsc(companyId: Long): List<RestaurantReservation>
    fun findAllByCompanyIdAndReservedAtBetweenOrderByReservedAtAsc(companyId: Long, startsAt: Instant, endsAt: Instant): List<RestaurantReservation>
    fun findByIdAndCompanyId(id: Long, companyId: Long): RestaurantReservation?
}
