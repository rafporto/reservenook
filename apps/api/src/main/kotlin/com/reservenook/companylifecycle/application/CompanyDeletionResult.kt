package com.reservenook.companylifecycle.application

data class CompanyDeletionResult(
    val deletedCompanies: Int,
    val failedDeletions: Int
)
