package com.reservenook.companylifecycle.application

import java.time.Instant

interface CompanyDeletionWarningMailSender {
    fun sendDeletionWarningEmail(email: String, companyName: String, deletionScheduledAt: Instant, language: String)
}
