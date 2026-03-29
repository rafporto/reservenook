package com.reservenook.auth.api

import com.reservenook.auth.application.LoginFailureCode

data class LoginErrorResponse(
    val message: String,
    val code: LoginFailureCode
)
