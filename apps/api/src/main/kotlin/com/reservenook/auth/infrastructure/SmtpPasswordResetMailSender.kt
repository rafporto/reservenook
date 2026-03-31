package com.reservenook.auth.infrastructure

import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.config.LocalizedEmailMessageFactory
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class SmtpPasswordResetMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer,
    private val localizedEmailMessageFactory: LocalizedEmailMessageFactory
) : PasswordResetMailSender {

    override fun sendPasswordResetEmail(email: String, resetLink: String, language: String) {
        val messageText = localizedEmailMessageFactory.passwordReset(language)
        val content = brandedEmailTemplateRenderer.render(
            title = messageText.title,
            intro = messageText.intro,
            actionLabel = messageText.actionLabel,
            actionUrl = resetLink,
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
