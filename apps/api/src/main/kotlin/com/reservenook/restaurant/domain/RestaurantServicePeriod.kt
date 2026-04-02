package com.reservenook.restaurant.domain

import com.reservenook.companybackoffice.domain.BusinessDay
import com.reservenook.registration.domain.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalTime

@Entity
@Table(name = "restaurant_service_periods")
class RestaurantServicePeriod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: BusinessDay,

    @Column(name = "opens_at", nullable = false)
    var opensAt: LocalTime,

    @Column(name = "closes_at", nullable = false)
    var closesAt: LocalTime,

    @Column(name = "slot_interval_minutes", nullable = false)
    var slotIntervalMinutes: Int,

    @Column(name = "reservation_duration_minutes", nullable = false)
    var reservationDurationMinutes: Int,

    @Column(name = "min_party_size", nullable = false)
    var minPartySize: Int,

    @Column(name = "max_party_size", nullable = false)
    var maxPartySize: Int,

    @Column(name = "booking_window_days", nullable = false)
    var bookingWindowDays: Int,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
