package com.reservenook.security.application

import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

@Service
class RequestThrottleService {

    private val attempts = ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>>()

    fun assertAllowed(
        scope: String,
        key: String,
        threshold: Int,
        window: Duration,
        now: Instant = Instant.now()
    ) {
        val bucketKey = bucketKey(scope, key)
        val bucket = attempts.computeIfAbsent(bucketKey) { ConcurrentLinkedDeque() }

        synchronized(bucket) {
            pruneExpired(bucket, window, now)
            if (bucket.size >= threshold) {
                throw TooManyRequestsException(messageFor(scope))
            }

            bucket.addLast(now)
        }
    }

    fun countActiveAttempts(
        scope: String,
        key: String,
        window: Duration,
        now: Instant = Instant.now()
    ): Int {
        val bucket = attempts[bucketKey(scope, key)] ?: return 0

        synchronized(bucket) {
            pruneExpired(bucket, window, now)
            return bucket.size
        }
    }

    fun clear(scope: String, key: String) {
        attempts.remove(bucketKey(scope, key))
    }

    fun clearAll() {
        attempts.clear()
    }

    private fun pruneExpired(bucket: ConcurrentLinkedDeque<Instant>, window: Duration, now: Instant) {
        val threshold = now.minus(window)
        while (true) {
            val oldest = bucket.peekFirst() ?: return
            if (!oldest.isBefore(threshold)) {
                return
            }

            bucket.pollFirst()
        }
    }

    private fun bucketKey(scope: String, key: String) = "$scope::$key"

    private fun messageFor(scope: String): String = when (scope) {
        "login" -> "Too many login attempts. Please wait and try again."
        "forgot-password" -> "Too many password reset requests. Please wait and try again."
        "resend-activation" -> "Too many activation email requests. Please wait and try again."
        else -> "Too many requests. Please wait and try again."
    }
}
