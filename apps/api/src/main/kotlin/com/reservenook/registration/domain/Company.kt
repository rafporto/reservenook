package com.reservenook.registration.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "companies")
class Company(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    var businessType: BusinessType,

    @Column(name = "slug", nullable = false, unique = true)
    var slug: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CompanyStatus,

    @Column(name = "default_language", nullable = false)
    var defaultLanguage: String,

    @Column(name = "default_locale", nullable = false)
    var defaultLocale: String,

    @Column(name = "last_activity_at", nullable = false)
    var lastActivityAt: Instant = Instant.now(),

    @Column(name = "inactive_at")
    var inactiveAt: Instant? = null,

    @Column(name = "deletion_scheduled_at")
    var deletionScheduledAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
