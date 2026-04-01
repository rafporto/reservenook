package com.reservenook.booking.domain

import com.reservenook.registration.domain.Company
import com.reservenook.security.domain.SecurityAuditOutcome
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

@Entity
@Table(name = "booking_audit_events")
class BookingAuditEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: Booking,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    var actionType: BookingAuditActionType,

    @Column(name = "actor_user_id")
    var actorUserId: Long? = null,

    @Column(name = "actor_email")
    var actorEmail: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    var outcome: SecurityAuditOutcome,

    @Column(name = "details")
    var details: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
