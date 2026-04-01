package com.reservenook.security.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "request_throttle_attempts",
    indexes = [
        Index(name = "idx_request_throttle_scope_key_occurred_at", columnList = "scope, bucket_key, occurred_at"),
        Index(name = "idx_request_throttle_occurred_at", columnList = "occurred_at")
    ]
)
class RequestThrottleAttempt(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "scope", nullable = false, length = 64)
    var scope: String,

    @Column(name = "bucket_key", nullable = false, length = 255)
    var bucketKey: String,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now()
)
