package com.reservenook.registration.infrastructure

import com.reservenook.registration.domain.Company
import org.springframework.data.jpa.repository.JpaRepository

interface CompanyRepository : JpaRepository<Company, Long> {
    fun existsBySlug(slug: String): Boolean
}
