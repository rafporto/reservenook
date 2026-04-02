package com.reservenook.widget.application

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class WidgetTokenClaims(
    val slug: String,
    val originHost: String,
    val expiresAtEpochSeconds: Long
)

@Service
class WidgetTokenService(
    @Value("\${app.widget.token-secret:dev-widget-secret-change-me}")
    private val tokenSecret: String,
    @Value("\${app.widget.token-ttl-seconds:900}")
    private val tokenTtlSeconds: Long
) {
    fun issue(slug: String, originHost: String): String {
        val expiresAt = Instant.now().plusSeconds(tokenTtlSeconds).epochSecond
        val payload = "$slug|$originHost|$expiresAt"
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray(StandardCharsets.UTF_8))
        val signature = sign(encodedPayload)
        return "$encodedPayload.$signature"
    }

    fun validate(expectedSlug: String, token: String?): WidgetTokenClaims? {
        if (token.isNullOrBlank()) {
            return null
        }
        val parts = token.split(".")
        if (parts.size != 2) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        }
        val payload = parts[0]
        if (sign(payload) != parts[1]) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        }
        val decoded = try {
            String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        }
        val segments = decoded.split("|")
        if (segments.size != 3) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        }
        val slug = segments[0]
        val originHost = segments[1]
        val expiresAt = segments[2].toLongOrNull() ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        if (slug != expectedSlug || expiresAt < Instant.now().epochSecond) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Widget token is invalid.")
        }
        return WidgetTokenClaims(slug = slug, originHost = originHost, expiresAtEpochSeconds = expiresAt)
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(tokenSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8)))
    }
}
