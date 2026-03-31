package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyDeletionWarningMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import java.time.Instant

class SmtpCompanyDeletionWarningMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
) : CompanyDeletionWarningMailSender {

    override fun sendDeletionWarningEmail(email: String, companyName: String, deletionScheduledAt: Instant) {
        val content = brandedEmailTemplateRenderer.render(
            title = "Your ReserveNook company is scheduled for deletion",
            intro = "Your company $companyName is scheduled for deletion on $deletionScheduledAt unless activity resumes before then.",
            actionLabel = "Review account",
            actionUrl = "${brandedEmailTemplateRenderer.publicBaseUrl}/en/login",
            footerNote = "This warning was sent because your company is within the configured deletion warning window."
        )
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(email)
        helper.setSubject("Your Reservenook company is scheduled for deletion")
        helper.setText(content.plainText, content.html)
        mailSender.send(message)
    }
}
