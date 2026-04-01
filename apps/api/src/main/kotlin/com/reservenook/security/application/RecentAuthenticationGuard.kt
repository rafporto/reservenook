package com.reservenook.security.application

import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class RecentAuthenticationGuard(
    @Value("\${app.security.recent-auth-window-seconds:900}")
    private val recentAuthWindowSeconds: Long
) {

    fun requireRecentAuthentication(session: HttpSession?) {
        val currentSession = session ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.")
        val nowMillis = System.currentTimeMillis()
        val recentAuthAtMillis = (currentSession.getAttribute(SessionSecurityAttributes.RECENT_AUTH_AT_MILLIS) as? Long)
            ?: currentSession.creationTime

        if (nowMillis - recentAuthAtMillis > recentAuthWindowSeconds * 1000) {
            throw RecentAuthenticationRequiredException()
        }
    }
}
