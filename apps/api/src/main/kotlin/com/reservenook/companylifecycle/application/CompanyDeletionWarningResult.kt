package com.reservenook.companylifecycle.application

data class CompanyDeletionWarningResult(
    val warningsSent: Int,
    val failedWarnings: Int
)
