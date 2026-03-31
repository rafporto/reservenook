package com.reservenook.registration.infrastructure

import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.config.LocalizedEmailMessageFactory
import jakarta.mail.internet.MimeMessage
import com.reservenook.registration.application.RegistrationMailSender
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class SmtpRegistrationMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer,
    private val localizedEmailMessageFactory: LocalizedEmailMessageFactory
) : RegistrationMailSender {

    override fun sendActivationEmail(email: String, activationLink: String, language: String) {
        val messageText = localizedEmailMessageFactory.activation(language)
        val content = brandedEmailTemplateRenderer.render(
            title = messageText.title,
            intro = messageText.intro,
            actionLabel = messageText.actionLabel,
            actionUrl = activationLink,
            footerNote = messageText.footerNote
        )
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, "UTF-8")
        helper.setTo(email)
        helper.setSubject(messageText.subject)
        helper.setText(content.plainText, content.html)
        mailSender.send(message)
    }
}
