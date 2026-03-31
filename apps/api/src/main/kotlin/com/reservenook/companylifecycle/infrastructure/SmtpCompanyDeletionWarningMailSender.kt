package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyDeletionWarningMailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.Instant

class SmtpCompanyDeletionWarningMailSender(
    private val mailSender: JavaMailSender
) : CompanyDeletionWarningMailSender {

    override fun sendDeletionWarningEmail(email: String, companyName: String, deletionScheduledAt: Instant) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "Your Reservenook company is scheduled for deletion"
        message.text = "Your company $companyName is scheduled for deletion on $deletionScheduledAt unless activity resumes before then."
        mailSender.send(message)
    }
}
