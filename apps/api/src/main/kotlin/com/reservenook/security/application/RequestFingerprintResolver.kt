package com.reservenook.security.application

import jakarta.servlet.http.HttpServletRequest

object RequestFingerprintResolver {

    fun resolve(request: HttpServletRequest, email: String): String =
        "${resolveClientAddress(request)}|${normalizeEmail(email)}"

    fun resolveClientAddress(request: HttpServletRequest): String =
        request.remoteAddr?.takeIf { it.isNotBlank() } ?: "unknown"

    fun normalizeEmail(email: String): String = email.trim().lowercase()
}
