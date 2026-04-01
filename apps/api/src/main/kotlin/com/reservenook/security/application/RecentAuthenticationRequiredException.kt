package com.reservenook.security.application

class RecentAuthenticationRequiredException(
    message: String = "Please sign in again before performing this sensitive action."
) : RuntimeException(message)
