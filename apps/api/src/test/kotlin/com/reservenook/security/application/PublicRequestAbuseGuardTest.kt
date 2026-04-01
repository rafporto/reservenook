package com.reservenook.security.application

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration

class PublicRequestAbuseGuardTest {

    private val requestThrottleService = mockk<RequestThrottleService>(relaxed = true)
    private val service = PublicRequestAbuseGuard(requestThrottleService)

    @Test
    fun `guard checks composite client and email buckets`() {
        service.assertAllowed("login", "127.0.0.1", "admin@acme.com")

        verify(exactly = 1) {
            requestThrottleService.assertAllowed("login", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), any())
        }
        verify(exactly = 1) {
            requestThrottleService.assertAllowed("login", "client::127.0.0.1", 10, Duration.ofMinutes(10), any())
        }
        verify(exactly = 1) {
            requestThrottleService.assertAllowed("login", "email::admin@acme.com", 10, Duration.ofMinutes(10), any())
        }
    }

    @Test
    fun `guard clears all login buckets after a successful login`() {
        service.clearSuccessfulLogin("127.0.0.1", "admin@acme.com")

        verify(exactly = 1) { requestThrottleService.clear("login", "127.0.0.1|admin@acme.com") }
        verify(exactly = 1) { requestThrottleService.clear("login", "client::127.0.0.1") }
        verify(exactly = 1) { requestThrottleService.clear("login", "email::admin@acme.com") }
    }
}
