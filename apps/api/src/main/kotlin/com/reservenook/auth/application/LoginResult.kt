package com.reservenook.auth.application

data class LoginResult(
    val authenticatedUser: AppAuthenticatedUser,
    val redirectTo: String
)
