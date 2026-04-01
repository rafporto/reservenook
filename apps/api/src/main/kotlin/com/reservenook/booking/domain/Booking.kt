package com.reservenook.booking.domain

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
import java.time.LocalDate

@Entity
@Table(name = "bookings")
class Booking(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_contact_id", nullable = false)
    var customerContact: CustomerContact,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: BookingStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    var source: BookingSource,

    @Column(name = "request_summary")
    var requestSummary: String? = null,

    @Column(name = "preferred_date")
    var preferredDate: LocalDate? = null,

    @Column(name = "internal_note")
    var internalNote: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
