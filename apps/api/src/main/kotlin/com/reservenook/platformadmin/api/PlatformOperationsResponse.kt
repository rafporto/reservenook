package com.reservenook.platformadmin.api

import com.reservenook.security.application.SecurityAuditRecordSummary
import com.reservenook.security.application.SecurityOperationsSummary

data class PlatformOperationsResponse(
    val summary: SecurityOperationsSummary,
    val securityAudit: List<SecurityAuditRecordSummary>
)

data class UpdateCompanyLegalHoldRequest(
    val legalHoldUntil: String?
)

data class UpdateCompanyLegalHoldResponse(
    val message: String,
    val companySlug: String,
    val legalHoldUntil: String?
)
