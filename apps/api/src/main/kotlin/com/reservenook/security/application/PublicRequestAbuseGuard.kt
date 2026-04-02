package com.reservenook.security.application

import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PublicRequestAbuseGuard(
    private val requestThrottleService: RequestThrottleService
) {

    fun assertClientAllowed(scope: String, clientAddress: String) {
        requestThrottleService.assertAllowed(scope, "client::$clientAddress", 20, Duration.ofMinutes(10))
    }

    fun assertAllowed(scope: String, clientAddress: String, normalizedEmail: String) {
        requestThrottleService.assertAllowed(scope, "$clientAddress|$normalizedEmail", 5, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed(scope, "client::$clientAddress", 10, Duration.ofMinutes(10))
        requestThrottleService.assertAllowed(scope, "email::$normalizedEmail", 10, Duration.ofMinutes(10))
    }

    fun clearSuccessfulLogin(clientAddress: String, normalizedEmail: String) {
        requestThrottleService.clear("login", "$clientAddress|$normalizedEmail")
        requestThrottleService.clear("login", "client::$clientAddress")
        requestThrottleService.clear("login", "email::$normalizedEmail")
    }
}
