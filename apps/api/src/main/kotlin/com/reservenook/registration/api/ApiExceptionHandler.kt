package com.reservenook.registration.api

import com.reservenook.auth.api.LoginErrorResponse
import com.reservenook.auth.application.LoginFailedException
import com.reservenook.auth.application.ResetPasswordFailedException
import com.reservenook.registration.application.RegistrationConflictException
import com.reservenook.security.application.RecentAuthenticationRequiredException
import com.reservenook.security.application.TooManyRequestsException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(LoginFailedException::class)
    fun handleLoginFailure(exception: LoginFailedException): ResponseEntity<LoginErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            LoginErrorResponse(
                message = "Invalid email or password.",
                code = com.reservenook.auth.application.LoginFailureCode.INVALID_CREDENTIALS.name
            )
        )
    }

    @ExceptionHandler(ResetPasswordFailedException::class)
    fun handleResetPasswordFailure(exception: ResetPasswordFailedException): ResponseEntity<LoginErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginErrorResponse(exception.message, exception.code.name))

    @ExceptionHandler(RegistrationConflictException::class)
    fun handleRegistrationConflict(exception: RegistrationConflictException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse(exception.message ?: "Registration conflict."))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(exception.message ?: "Invalid request."))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationFailure(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val firstError = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Validation failed."
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(firstError))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolations(exception: ConstraintViolationException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(exception.message ?: "Validation failed."))

    @ExceptionHandler(TooManyRequestsException::class)
    fun handleTooManyRequests(exception: TooManyRequestsException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
            ApiErrorResponse(exception.message ?: "Too many requests. Please wait and try again.")
        )

    @ExceptionHandler(RecentAuthenticationRequiredException::class)
    fun handleRecentAuthenticationRequired(exception: RecentAuthenticationRequiredException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiErrorResponse(exception.message ?: "Please sign in again before performing this sensitive action.")
        )

    @ExceptionHandler(MailException::class)
    fun handleMailFailure(): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ApiErrorResponse(
                "Registration could not be completed because the activation email could not be sent."
            )
        )
}
