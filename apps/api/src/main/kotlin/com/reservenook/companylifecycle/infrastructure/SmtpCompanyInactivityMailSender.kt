package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyInactivityMailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

class SmtpCompanyInactivityMailSender(
    private val mailSender: JavaMailSender
) : CompanyInactivityMailSender {

    override fun sendInactivityEmail(email: String, companyName: String) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "Your Reservenook company is inactive"
        message.text = "Your company $companyName has been marked inactive. Sign in to review the account state."
        mailSender.send(message)
    }
}
