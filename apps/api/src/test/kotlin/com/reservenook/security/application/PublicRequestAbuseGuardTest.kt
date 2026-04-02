package com.reservenook.security.application

import com.reservenook.platformadmin.application.AbusePreventionPolicyService
import com.reservenook.platformadmin.domain.AbusePreventionPolicy
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration

class PublicRequestAbuseGuardTest {

    private val requestThrottleService = mockk<RequestThrottleService>(relaxed = true)
    private val abusePreventionPolicyService = mockk<AbusePreventionPolicyService>()
    private val service = PublicRequestAbuseGuard(requestThrottleService, abusePreventionPolicyService)

    init {
        every { abusePreventionPolicyService.getSystemPolicy() } returns AbusePreventionPolicy(
            loginPairLimit = 5,
            loginClientLimit = 10,
            loginEmailLimit = 10,
            publicWritePairLimit = 5,
            publicWriteClientLimit = 10,
            publicWriteEmailLimit = 10,
            publicReadClientLimit = 20
        )
    }

    @Test
    fun `guard checks composite client and email buckets`() {
        service.assertAllowed("forgot-password", "127.0.0.1", "admin@acme.com")

        verify(exactly = 1) {
            requestThrottleService.assertAllowed("forgot-password", "127.0.0.1|admin@acme.com", 5, Duration.ofMinutes(10), any())
        }
        verify(exactly = 1) {
            requestThrottleService.assertAllowed("forgot-password", "client::127.0.0.1", 10, Duration.ofMinutes(10), any())
        }
        verify(exactly = 1) {
            requestThrottleService.assertAllowed("forgot-password", "email::admin@acme.com", 10, Duration.ofMinutes(10), any())
        }
    }

    @Test
    fun `login guard uses login specific thresholds`() {
        service.assertLoginAllowed("127.0.0.1", "admin@acme.com")

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
    fun `client only guard uses public read threshold`() {
        service.assertClientAllowed("appointment-availability", "127.0.0.1")

        verify(exactly = 1) {
            requestThrottleService.assertAllowed("appointment-availability", "client::127.0.0.1", 20, Duration.ofMinutes(10), any())
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
