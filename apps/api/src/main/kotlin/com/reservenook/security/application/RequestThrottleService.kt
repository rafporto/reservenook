package com.reservenook.security.application

import com.reservenook.security.domain.RequestThrottleAttempt
import com.reservenook.security.infrastructure.RequestThrottleAttemptRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class RequestThrottleService(
    private val requestThrottleAttemptRepository: RequestThrottleAttemptRepository
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun assertAllowed(
        scope: String,
        key: String,
        threshold: Int,
        window: Duration,
        now: Instant = Instant.now()
    ) {
        val bucketKey = bucketKey(scope, key)
        pruneExpired(window, now)
        val activeAttempts = requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
            scope,
            bucketKey,
            now.minus(window)
        )
        if (activeAttempts >= threshold) {
            throw TooManyRequestsException(messageFor(scope))
        }

        requestThrottleAttemptRepository.save(
            RequestThrottleAttempt(
                scope = scope,
                bucketKey = bucketKey,
                occurredAt = now
            )
        )
    }

    @Transactional(readOnly = true)
    fun countActiveAttempts(
        scope: String,
        key: String,
        window: Duration,
        now: Instant = Instant.now()
    ): Int {
        pruneExpired(window, now)
        return requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
            scope,
            bucketKey(scope, key),
            now.minus(window)
        ).toInt()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun clear(scope: String, key: String) {
        requestThrottleAttemptRepository.deleteByScopeAndBucketKey(scope, bucketKey(scope, key))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun clearAll() {
        requestThrottleAttemptRepository.deleteAll()
    }

    private fun pruneExpired(window: Duration, now: Instant) {
        requestThrottleAttemptRepository.deleteByOccurredAtBefore(now.minus(window))
    }

    private fun bucketKey(scope: String, key: String) = "$scope::$key"

    private fun messageFor(scope: String): String = when (scope) {
        "login" -> "Too many login attempts. Please wait and try again."
        "forgot-password" -> "Too many password reset requests. Please wait and try again."
        "resend-activation" -> "Too many activation email requests. Please wait and try again."
        else -> "Too many requests. Please wait and try again."
    }
}
