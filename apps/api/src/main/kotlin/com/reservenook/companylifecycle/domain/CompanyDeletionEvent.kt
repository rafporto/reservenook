package com.reservenook.companylifecycle.domain

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
@Table(name = "company_deletion_events")
class CompanyDeletionEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "company_id", nullable = false)
    var companyId: Long,

    @Column(name = "company_name", nullable = false)
    var companyName: String,

    @Column(name = "company_slug", nullable = false)
    var companySlug: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CompanyDeletionEventStatus,

    @Column(name = "failure_reason")
    var failureReason: String? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
