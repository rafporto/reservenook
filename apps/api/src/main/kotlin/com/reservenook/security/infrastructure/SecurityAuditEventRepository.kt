package com.reservenook.security.infrastructure

import com.reservenook.security.domain.SecurityAuditEvent
import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository

interface SecurityAuditEventRepository : JpaRepository<SecurityAuditEvent, Long> {
    fun findTop100ByOrderByCreatedAtDesc(): List<SecurityAuditEvent>
    fun findTop100ByCompanySlugOrderByCreatedAtDesc(companySlug: String): List<SecurityAuditEvent>
    fun findAllByCreatedAtAfter(createdAt: Instant): List<SecurityAuditEvent>
    fun findAllByCompanySlugAndCreatedAtAfter(companySlug: String, createdAt: Instant): List<SecurityAuditEvent>
}
