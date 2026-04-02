package com.reservenook.shared.validation

object CommonInputValidation {

    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val phonePattern = Regex("^[0-9+()\\-\\s]{7,}$")
    private val domainPattern = Regex("^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z]{2,}$")

    fun requireEmail(value: String, message: String): String {
        val normalized = value.trim().lowercase()
        if (!emailPattern.matches(normalized)) {
            throw IllegalArgumentException(message)
        }
        return normalized
    }

    fun requireOptionalPhone(value: String?, message: String): String? {
        val normalized = value?.trim()?.ifBlank { null } ?: return null
        if (!phonePattern.matches(normalized)) {
            throw IllegalArgumentException(message)
        }
        return normalized
    }

    fun requireDomains(values: List<String>, message: String): List<String> {
        val normalized = values.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        normalized.forEach {
            if (!domainPattern.matches(it)) {
                throw IllegalArgumentException(message)
            }
        }
        return normalized
    }
}
