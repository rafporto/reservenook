package com.reservenook.registration.infrastructure

import com.reservenook.config.BrandedEmailTemplateRenderer
import jakarta.mail.internet.MimeMessage
import com.reservenook.registration.application.RegistrationMailSender
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class SmtpRegistrationMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
) : RegistrationMailSender {

    override fun sendActivationEmail(email: String, activationLink: String) {
        val content = brandedEmailTemplateRenderer.render(
            title = "Activate your ReserveNook account",
            intro = "Confirm your company account to start using ReserveNook.",
            actionLabel = "Activate account",
            actionUrl = activationLink
        )
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(email)
        helper.setSubject("Activate your Reservenook account")
        helper.setText(content.plainText, content.html)
        mailSender.send(message)
    }
}
