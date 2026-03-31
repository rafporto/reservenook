package com.reservenook.auth.infrastructure

import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class SmtpPasswordResetMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
) : PasswordResetMailSender {

    override fun sendPasswordResetEmail(email: String, resetLink: String) {
        val content = brandedEmailTemplateRenderer.render(
            title = "Reset your ReserveNook password",
            intro = "Use the secure link below to choose a new password for your ReserveNook account.",
            actionLabel = "Reset password",
            actionUrl = resetLink
        )
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(email)
        helper.setSubject("Reset your Reservenook password")
        helper.setText(content.plainText, content.html)
        mailSender.send(message)
    }
}
