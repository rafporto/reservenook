package com.reservenook.groupclass.domain

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

@Entity
@Table(name = "class_sessions")
class ClassSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_type_id", nullable = false)
    var classType: ClassType,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    var instructor: ClassInstructor,

    @Column(name = "starts_at", nullable = false)
    var startsAt: Instant,

    @Column(name = "ends_at", nullable = false)
    var endsAt: Instant,

    @Column(name = "capacity", nullable = false)
    var capacity: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ClassSessionStatus = ClassSessionStatus.SCHEDULED,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
