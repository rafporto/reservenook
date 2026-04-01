package com.reservenook.booking.domain

import com.reservenook.registration.domain.Company
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "customer_contacts")
class CustomerContact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "normalized_email", nullable = false)
    var normalizedEmail: String,

    @Column(name = "phone")
    var phone: String? = null,

    @Column(name = "preferred_language")
    var preferredLanguage: String? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
