package com.reservenook.appointment.domain

import com.reservenook.companybackoffice.domain.BusinessDay
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
import java.time.LocalTime

@Entity
@Table(name = "appointment_provider_availability")
class AppointmentProviderAvailability(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    var provider: AppointmentProvider,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    var dayOfWeek: BusinessDay,

    @Column(name = "opens_at", nullable = false)
    var opensAt: LocalTime,

    @Column(name = "closes_at", nullable = false)
    var closesAt: LocalTime,

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int
)
