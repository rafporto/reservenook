package com.reservenook.security.application

import com.reservenook.platformadmin.application.AbusePreventionPolicyService
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PublicRequestAbuseGuard(
    private val requestThrottleService: RequestThrottleService,
    private val abusePreventionPolicyService: AbusePreventionPolicyService
) {

    fun assertClientAllowed(scope: String, clientAddress: String) {
        val policy = abusePreventionPolicyService.getSystemPolicy()
        requestThrottleService.assertAllowed(scope, "client::$clientAddress", policy.publicReadClientLimit, Duration.ofMinutes(10))
    }

    fun assertAllowed(scope: String, clientAddress: String, normalizedEmail: String) {
        val policy = abusePreventionPolicyService.getSystemPolicy()
        requestThrottleService.assertAllowed(scope, "$clientAddress|$normalizedEmail", policy.publicWritePairLimit, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed(scope, "client::$clientAddress", policy.publicWriteClientLimit, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed(scope, "email::$normalizedEmail", policy.publicWriteEmailLimit, Duration.ofMinutes(10))
    }

    fun assertLoginAllowed(clientAddress: String, normalizedEmail: String) {
        val policy = abusePreventionPolicyService.getSystemPolicy()
        requestThrottleService.assertAllowed("login", "$clientAddress|$normalizedEmail", policy.loginPairLimit, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed("login", "client::$clientAddress", policy.loginClientLimit, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed("login", "email::$normalizedEmail", policy.loginEmailLimit, Duration.ofMinutes(10))
    }

    fun clearSuccessfulLogin(clientAddress: String, normalizedEmail: String) {
        requestThrottleService.clear("login", "$clientAddress|$normalizedEmail")
        requestThrottleService.clear("login", "client::$clientAddress")
        requestThrottleService.clear("login", "email::$normalizedEmail")
    }
}
