package com.reservenook.restaurant.infrastructure

import com.reservenook.restaurant.domain.RestaurantReservationTable
import org.springframework.data.jpa.repository.JpaRepository

interface RestaurantReservationTableRepository : JpaRepository<RestaurantReservationTable, Long> {
    fun findAllByReservationId(reservationId: Long): List<RestaurantReservationTable>
}
