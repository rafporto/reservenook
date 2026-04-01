package com.reservenook.security.infrastructure

import com.reservenook.security.domain.SecurityAuditEvent
import org.springframework.data.jpa.repository.JpaRepository

interface SecurityAuditEventRepository : JpaRepository<SecurityAuditEvent, Long>
