package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyInactivityMailSender

class NoopCompanyInactivityMailSender : CompanyInactivityMailSender {

    override fun sendInactivityEmail(email: String, companyName: String) {
        // Intentionally no-op when SMTP is not configured for the current environment.
    }
}
