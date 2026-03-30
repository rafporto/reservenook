package com.reservenook.platformadmin.infrastructure

import com.reservenook.platformadmin.domain.InactivityPolicy
import org.springframework.data.jpa.repository.JpaRepository

interface InactivityPolicyRepository : JpaRepository<InactivityPolicy, Long>
