package com.reservenook.restaurant.domain

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
@Table(name = "restaurant_tables")
class RestaurantTable(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dining_area_id", nullable = false)
    var diningArea: DiningArea,

    @Column(name = "label", nullable = false)
    var label: String,

    @Column(name = "min_party_size", nullable = false)
    var minPartySize: Int,

    @Column(name = "max_party_size", nullable = false)
    var maxPartySize: Int,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
)
