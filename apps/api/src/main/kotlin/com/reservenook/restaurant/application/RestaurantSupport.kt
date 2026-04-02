package com.reservenook.restaurant.application

import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.registration.domain.BusinessType
import com.reservenook.restaurant.domain.DiningArea
import com.reservenook.restaurant.domain.RestaurantReservation
import com.reservenook.restaurant.domain.RestaurantReservationStatus
import com.reservenook.restaurant.domain.RestaurantServicePeriod
import com.reservenook.restaurant.domain.RestaurantTable
import com.reservenook.restaurant.domain.RestaurantTableCombination
import org.springframework.web.server.ResponseStatusException
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

data class RestaurantTableGroup(
    val tableIds: List<Long>,
    val labels: List<String>,
    val areaNames: List<String>,
    val minPartySize: Int,
    val maxPartySize: Int
)

object RestaurantSupport {
    fun requireRestaurantName(value: String, field: String): String =
        value.trim().ifBlank { throw IllegalArgumentException("$field is required.") }

    fun requirePositive(value: Int, field: String): Int =
        value.takeIf { it > 0 } ?: throw IllegalArgumentException("$field must be greater than zero.")

    fun requireCapacityRange(minPartySize: Int, maxPartySize: Int): Pair<Int, Int> {
        val min = requirePositive(minPartySize, "Minimum party size")
        val max = requirePositive(maxPartySize, "Maximum party size")
        if (max < min) {
            throw IllegalArgumentException("Maximum party size must be greater than or equal to the minimum party size.")
        }
        return min to max
    }

    fun parseDay(value: String): BusinessDay = try {
        BusinessDay.valueOf(value.trim().uppercase())
    } catch (_: IllegalArgumentException) {
        throw IllegalArgumentException("Day of week is invalid.")
    }

    fun parseTime(value: String, field: String): LocalTime = try {
        LocalTime.parse(value.trim())
    } catch (_: Exception) {
        throw IllegalArgumentException("$field must use HH:mm format.")
    }

    fun parseInstant(value: String, field: String): Instant = try {
        Instant.parse(value.trim())
    } catch (_: Exception) {
        throw IllegalArgumentException("$field must be a valid ISO timestamp.")
    }

    fun parseDate(value: String, field: String): LocalDate = try {
        LocalDate.parse(value.trim())
    } catch (_: Exception) {
        throw IllegalArgumentException("$field must use YYYY-MM-DD format.")
    }

    fun businessDayFor(date: LocalDate): BusinessDay = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> BusinessDay.MONDAY
        DayOfWeek.TUESDAY -> BusinessDay.TUESDAY
        DayOfWeek.WEDNESDAY -> BusinessDay.WEDNESDAY
        DayOfWeek.THURSDAY -> BusinessDay.THURSDAY
        DayOfWeek.FRIDAY -> BusinessDay.FRIDAY
        DayOfWeek.SATURDAY -> BusinessDay.SATURDAY
        DayOfWeek.SUNDAY -> BusinessDay.SUNDAY
    }

    fun validateServicePeriod(period: RestaurantServicePeriod) {
        if (!period.opensAt.isBefore(period.closesAt)) {
            throw IllegalArgumentException("Service period closing time must be after opening time.")
        }
        requirePositive(period.slotIntervalMinutes, "Slot interval")
        requirePositive(period.reservationDurationMinutes, "Reservation duration")
        requirePositive(period.bookingWindowDays, "Booking window")
        requireCapacityRange(period.minPartySize, period.maxPartySize)
    }

    fun buildTableGroups(
        tables: List<RestaurantTable>,
        combinations: List<RestaurantTableCombination>
    ): List<RestaurantTableGroup> {
        val byId = tables.associateBy { requireNotNull(it.id) }
        val singles = tables
            .filter { it.active }
            .map {
                RestaurantTableGroup(
                    tableIds = listOf(requireNotNull(it.id)),
                    labels = listOf(it.label),
                    areaNames = listOf(it.diningArea.name),
                    minPartySize = it.minPartySize,
                    maxPartySize = it.maxPartySize
                )
            }
        val pairs = combinations.mapNotNull { combination ->
            val primary = byId[requireNotNull(combination.primaryTable.id)]
            val secondary = byId[requireNotNull(combination.secondaryTable.id)]
            if (primary == null || secondary == null || !primary.active || !secondary.active) {
                null
            } else {
                RestaurantTableGroup(
                    tableIds = listOf(requireNotNull(primary.id), requireNotNull(secondary.id)).sorted(),
                    labels = listOf(primary.label, secondary.label),
                    areaNames = listOf(primary.diningArea.name, secondary.diningArea.name).distinct(),
                    minPartySize = primary.minPartySize + secondary.minPartySize,
                    maxPartySize = primary.maxPartySize + secondary.maxPartySize
                )
            }
        }
        return (singles + pairs).distinctBy { it.tableIds }
    }

    fun overlaps(startA: Instant, endA: Instant, startB: Instant, endB: Instant): Boolean =
        startA < endB && startB < endA

    fun candidateSlots(date: LocalDate, period: RestaurantServicePeriod): List<Instant> {
        val start = LocalDateTime.of(date, period.opensAt)
        val latest = LocalDateTime.of(date, period.closesAt).minusMinutes(period.reservationDurationMinutes.toLong())
        val entries = mutableListOf<Instant>()
        var current = start
        while (!current.isAfter(latest)) {
            entries += current.toInstant(ZoneOffset.UTC)
            current = current.plusMinutes(period.slotIntervalMinutes.toLong())
        }
        return entries
    }

    fun activeStatuses(): Set<RestaurantReservationStatus> = setOf(
        RestaurantReservationStatus.CONFIRMED,
        RestaurantReservationStatus.SEATED
    )

    fun findAssignableGroup(
        reservationStart: Instant,
        reservationEnd: Instant,
        partySize: Int,
        reservations: List<RestaurantReservation>,
        tableGroups: List<RestaurantTableGroup>
    ): RestaurantTableGroup? {
        val occupiedTableIds = reservations
            .filter { it.status in activeStatuses() && overlaps(reservationStart, reservationEnd, it.reservedAt, it.reservedUntil) }
            .flatMap { reservation -> reservation.tableAssignments.map { assignment -> requireNotNull(assignment.restaurantTable.id) } }
            .toSet()

        return tableGroups
            .filter { partySize in it.minPartySize..it.maxPartySize }
            .sortedWith(compareBy<RestaurantTableGroup> { it.tableIds.size }.thenBy { it.maxPartySize })
            .firstOrNull { group -> group.tableIds.none { it in occupiedTableIds } }
    }

    fun requireRestaurantBusinessType(actual: BusinessType) {
        if (actual != BusinessType.RESTAURANT) {
            throw ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Restaurant reservations are unavailable.")
        }
    }

    fun requireArea(area: DiningArea?, message: String = "Dining area could not be found."): DiningArea =
        area ?: throw ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, message)
}
