package com.reservenook.companylifecycle.application

data class InactivityNotificationResult(
    val companiesNotified: Int,
    val failedNotifications: Int
)
