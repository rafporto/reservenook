package com.reservenook.auth.application

class ResetPasswordFailedException(
    override val message: String,
    val code: ResetPasswordFailureCode
) : RuntimeException(message)
