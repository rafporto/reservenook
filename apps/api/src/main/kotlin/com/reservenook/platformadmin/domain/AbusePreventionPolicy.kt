package com.reservenook.platformadmin.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "abuse_prevention_policies")
class AbusePreventionPolicy(
    @Id
    var id: Long = 1L,

    @Column(name = "login_pair_limit", nullable = false)
    var loginPairLimit: Int,

    @Column(name = "login_client_limit", nullable = false)
    var loginClientLimit: Int,

    @Column(name = "login_email_limit", nullable = false)
    var loginEmailLimit: Int,

    @Column(name = "public_write_pair_limit", nullable = false)
    var publicWritePairLimit: Int,

    @Column(name = "public_write_client_limit", nullable = false)
    var publicWriteClientLimit: Int,

    @Column(name = "public_write_email_limit", nullable = false)
    var publicWriteEmailLimit: Int,

    @Column(name = "public_read_client_limit", nullable = false)
    var publicReadClientLimit: Int,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
