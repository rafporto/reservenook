package com.reservenook.restaurant.domain

import com.reservenook.booking.domain.Booking
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
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "restaurant_reservations")
class RestaurantReservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    var booking: Booking,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_period_id", nullable = false)
    var servicePeriod: RestaurantServicePeriod,

    @Column(name = "reserved_at", nullable = false)
    var reservedAt: Instant,

    @Column(name = "reserved_until", nullable = false)
    var reservedUntil: Instant,

    @Column(name = "party_size", nullable = false)
    var partySize: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: RestaurantReservationStatus,

    @OneToMany(mappedBy = "reservation", orphanRemoval = true)
    var tableAssignments: MutableList<RestaurantReservationTable> = mutableListOf(),

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
