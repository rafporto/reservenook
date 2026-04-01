package com.reservenook.security.application

import jakarta.servlet.http.HttpServletRequest

object RequestFingerprintResolver {

    fun resolve(request: HttpServletRequest, email: String): String {
        val forwardedFor = request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        val clientAddress = forwardedFor ?: request.remoteAddr ?: "unknown"
        return "$clientAddress|${email.trim().lowercase()}"
    }
}
