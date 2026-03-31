package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyInactivityMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

class SmtpCompanyInactivityMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
) : CompanyInactivityMailSender {

    override fun sendInactivityEmail(email: String, companyName: String) {
        val content = brandedEmailTemplateRenderer.render(
            title = "Your ReserveNook company is inactive",
            intro = "Your company $companyName has been marked inactive. Sign in to review the account state.",
            actionLabel = "Sign in",
            actionUrl = "${brandedEmailTemplateRenderer.publicBaseUrl}/en/login",
            footerNote = "This notice was sent because your company has entered the inactive lifecycle state."
        )
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(email)
        helper.setSubject("Your Reservenook company is inactive")
        helper.setText(content.plainText, content.html)
        mailSender.send(message)
    }
}
