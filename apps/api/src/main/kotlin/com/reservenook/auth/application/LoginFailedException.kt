package com.reservenook.auth.application

class LoginFailedException(
    override val message: String,
    val code: LoginFailureCode
) : RuntimeException(message)
