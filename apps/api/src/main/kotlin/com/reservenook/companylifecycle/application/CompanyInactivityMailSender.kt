package com.reservenook.companylifecycle.application

interface CompanyInactivityMailSender {
    fun sendInactivityEmail(email: String, companyName: String)
}
