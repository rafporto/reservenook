package com.reservenook.security.application

import com.reservenook.security.domain.RequestThrottleAttempt
import com.reservenook.security.infrastructure.RequestThrottleAttemptRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class RequestThrottleServiceTest {

    private val requestThrottleAttemptRepository = mockk<RequestThrottleAttemptRepository>(relaxed = true)
    private val service = RequestThrottleService(requestThrottleAttemptRepository)

    init {
        every { requestThrottleAttemptRepository.save(any<RequestThrottleAttempt>()) } answers { firstArg() }
    }

    @Test
    fun `allows requests inside the configured threshold`() {
        val baseTime = Instant.parse("2026-04-01T10:00:00Z")
        every {
            requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
                "login",
                "login::127.0.0.1|admin@acme.com",
                any()
            )
        } returnsMany listOf(0L, 1L)

        service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime)
        service.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(5))

        every {
            requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
                "login",
                "login::127.0.0.1|admin@acme.com",
                any()
            )
        } returns 2L

        service.countActiveAttempts("login", "127.0.0.1|admin@acme.com", Duration.ofMinutes(10), baseTime.plusSeconds(5)) shouldBe 2
        verify(exactly = 2) { requestThrottleAttemptRepository.save(any<RequestThrottleAttempt>()) }
    }

    @Test
    fun `blocks requests that exceed the threshold inside the window`() {
        val baseTime = Instant.parse("2026-04-01T10:00:00Z")
        every {
            requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
                "login",
                "login::127.0.0.1|admin@acme.com",
                any()
            )
        } returnsMany listOf(0L, 1L, 2L, 3L, 4L, 5L)
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
        every {
            requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
                "forgot-password",
                "forgot-password::127.0.0.1|admin@acme.com",
                any()
            )
        } returnsMany listOf(0L, 1L, 2L, 3L, 4L, 0L)
        repeat(5) { attempt ->
            service.assertAllowed("forgot-password", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plusSeconds(attempt.toLong()))
        }

        service.assertAllowed("forgot-password", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), baseTime.plus(Duration.ofMinutes(11)))

        every {
            requestThrottleAttemptRepository.countByScopeAndBucketKeyAndOccurredAtAfter(
                "forgot-password",
                "forgot-password::127.0.0.1|admin@acme.com",
                any()
            )
        } returns 1L

        service.countActiveAttempts("forgot-password", "127.0.0.1|admin@acme.com", Duration.ofMinutes(10), baseTime.plus(Duration.ofMinutes(11))) shouldBe 1
        verify(atLeast = 1) { requestThrottleAttemptRepository.deleteByOccurredAtBefore(any()) }
    }
}
