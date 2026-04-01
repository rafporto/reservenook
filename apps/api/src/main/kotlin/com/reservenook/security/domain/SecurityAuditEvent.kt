package com.reservenook.security.domain

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
@Table(name = "security_audit_events")
class SecurityAuditEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    var eventType: SecurityAuditEventType,

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    var outcome: SecurityAuditOutcome,

    @Column(name = "actor_user_id")
    var actorUserId: Long? = null,

    @Column(name = "actor_email")
    var actorEmail: String? = null,

    @Column(name = "company_slug")
    var companySlug: String? = null,

    @Column(name = "target_email")
    var targetEmail: String? = null,

    @Column(name = "details")
    var details: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now()
)
