package com.reservenook.platformadmin.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "inactivity_policies")
class InactivityPolicy(
    @Id
    var id: Long = 1,

    @Column(name = "inactivity_threshold_days", nullable = false)
    var inactivityThresholdDays: Int,

    @Column(name = "deletion_warning_lead_days", nullable = false)
    var deletionWarningLeadDays: Int,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
