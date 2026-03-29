package com.reservenook.registration.domain

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
@Table(name = "company_subscriptions")
class CompanySubscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    var planType: PlanType,

    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant,

    @Column(name = "expires_at", nullable = false)
    var expiresAt: Instant
)
