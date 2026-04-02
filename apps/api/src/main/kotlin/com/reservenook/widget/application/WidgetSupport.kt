package com.reservenook.widget.application

import com.reservenook.registration.domain.Company
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.net.URI

object WidgetSupport {
    fun extractOriginHost(origin: String?): String {
        val normalized = origin?.trim()?.takeIf { it.isNotBlank() }
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget origin is required.")
        val uri = try {
            URI(normalized)
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget origin is invalid.")
        }
        return uri.host?.lowercase()
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget origin is invalid.")
    }

    fun allowedHosts(company: Company): Set<String> =
        company.widgetAllowedDomains
            ?.split("\n")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?.toSet()
            ?: emptySet()

    fun requireAllowedOrigin(company: Company, origin: String?): String {
        if (!company.widgetEnabled) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Widget is unavailable.")
        }
        val host = extractOriginHost(origin)
        if (host !in allowedHosts(company)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget origin is not allowed.")
        }
        return host
    }
}
