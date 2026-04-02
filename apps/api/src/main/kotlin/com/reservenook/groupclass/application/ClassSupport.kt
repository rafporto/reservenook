package com.reservenook.groupclass.application

import com.reservenook.shared.validation.CommonInputValidation
import java.time.Instant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun requireClassName(value: String): String =
    value.trim().ifBlank { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class name is required.") }

fun requireInstructorName(value: String): String =
    value.trim().ifBlank { throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Instructor name is required.") }

fun requirePositiveDuration(durationMinutes: Int): Int {
    if (durationMinutes <= 0) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be positive.")
    }
    return durationMinutes
}

fun requirePositiveCapacity(capacity: Int): Int {
    if (capacity <= 0) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Capacity must be positive.")
    }
    return capacity
}

fun parseRequiredInstant(value: String, fieldName: String): Instant = try {
    Instant.parse(value)
} catch (_: Exception) {
    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$fieldName must be a valid ISO timestamp.")
}

fun validateSessionWindow(startsAt: Instant, endsAt: Instant) {
    if (!endsAt.isAfter(startsAt)) {
        throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Class session end time must be after the start time.")
    }
}

fun normalizeOptionalEmail(value: String?): String? =
    value?.trim()?.ifBlank { null }?.let { CommonInputValidation.requireEmail(it, "Instructor email must be valid.") }
