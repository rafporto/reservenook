package com.reservenook.restaurant.domain

import com.reservenook.registration.domain.Company
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
@Table(name = "restaurant_table_combinations")
class RestaurantTableCombination(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    var company: Company,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_table_id", nullable = false)
    var primaryTable: RestaurantTable,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "secondary_table_id", nullable = false)
    var secondaryTable: RestaurantTable,

    var createdAt: Instant = Instant.now()
)
