package com.reservenook.groupclass.domain

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
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "class_bookings")
class ClassBooking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    var booking: Booking,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_session_id", nullable = false)
    var classSession: ClassSession,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ClassBookingStatus,

    @Column(name = "waitlist_position")
    var waitlistPosition: Int? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
