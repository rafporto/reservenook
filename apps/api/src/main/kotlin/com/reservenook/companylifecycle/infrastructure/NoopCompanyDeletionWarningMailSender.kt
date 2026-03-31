package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyDeletionWarningMailSender
import java.time.Instant

class NoopCompanyDeletionWarningMailSender : CompanyDeletionWarningMailSender {

    override fun sendDeletionWarningEmail(email: String, companyName: String, deletionScheduledAt: Instant, language: String) {
        // Intentionally no-op when SMTP is not configured for the current environment.
    }
}
