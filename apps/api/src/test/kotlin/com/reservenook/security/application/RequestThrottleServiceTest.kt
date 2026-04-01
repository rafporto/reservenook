package com.reservenook.security.application

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class RequestThrottleServiceTest {

    private val service = RequestThrottleService()

    @Test
    fun `allows requests inside the configured threshold`() {
        val baseTime = Instant.parse("2026-04-01T10:00:00Z")

        service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime)
        service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(5))

        service.countActiveAttempts("login", "127.0.0.1|admin@acme.com", Duration.ofMinutes(10), baseTime.plusSeconds(5)) shouldBe 2
    }

    @Test
    fun `blocks requests that exceed the threshold inside the window`() {
        val baseTime = Instant.parse("2026-04-01T10:00:00Z")
        repeat(5) { attempt ->
            service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(attempt.toLong()))
        }

        val exception = org.junit.jupiter.api.assertThrows<TooManyRequestsException> {
            service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(30))
        }

        exception.message shouldBe "Too many login attempts. Please wait and try again."
    }

    @Test
    fun `expired attempts fall out of the active window`() {
        val baseTime = Instant.parse("2026-04-01T10:00:00Z")
        repeat(5) { attempt ->
            service.assertAllowed("forgot-password", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(attempt.toLong()))
        }

        service.assertAllowed("forgot-password", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plus(Duration.ofMinutes(11)))

        service.countActiveAttempts("forgot-password", "127.0.0.1|admin@acme.com", Duration.ofMinutes(10), baseTime.plus(Duration.ofMinutes(11))) shouldBe 1
    }
}
