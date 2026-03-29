package com.reservenook.registration.api

import com.reservenook.auth.api.LoginErrorResponse
import com.reservenook.auth.application.LoginFailedException
import com.reservenook.registration.application.RegistrationConflictException
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
        val status = when (exception.code) {
            com.reservenook.auth.application.LoginFailureCode.INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED
            com.reservenook.auth.application.LoginFailureCode.ACTIVATION_REQUIRED -> HttpStatus.FORBIDDEN
            com.reservenook.auth.application.LoginFailureCode.INACTIVE_COMPANY -> HttpStatus.FORBIDDEN
        }

        return ResponseEntity.status(status).body(LoginErrorResponse(exception.message, exception.code))
    }

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

    @ExceptionHandler(MailException::class)
    fun handleMailFailure(): ResponseEntity<ApiErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            ApiErrorResponse(
                "Registration could not be completed because the activation email could not be sent."
            )
        )
}
