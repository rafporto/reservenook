package com.reservenook.platformadmin.infrastructure

import com.reservenook.platformadmin.domain.AbusePreventionPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface AbusePreventionPolicyRepository : JpaRepository<AbusePreventionPolicy, Long>
