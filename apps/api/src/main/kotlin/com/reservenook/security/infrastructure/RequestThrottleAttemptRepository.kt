package com.reservenook.security.infrastructure

import com.reservenook.security.domain.RequestThrottleAttempt
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface RequestThrottleAttemptRepository : JpaRepository<RequestThrottleAttempt, Long> {
    fun countByScopeAndBucketKeyAndOccurredAtAfter(scope: String, bucketKey: String, occurredAt: Instant): Long
    fun deleteByScopeAndBucketKey(scope: String, bucketKey: String)
    fun deleteByOccurredAtBefore(occurredAt: Instant)
}
