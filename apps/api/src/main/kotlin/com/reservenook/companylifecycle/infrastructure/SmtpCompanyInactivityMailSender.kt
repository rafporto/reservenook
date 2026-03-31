package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyInactivityMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.config.LocalizedEmailMessageFactory
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

class SmtpCompanyInactivityMailSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer,
    private val localizedEmailMessageFactory: LocalizedEmailMessageFactory
) : CompanyInactivityMailSender {

    override fun sendInactivityEmail(email: String, companyName: String, language: String) {
        val normalizedLanguage = language.trim().lowercase().ifBlank { "en" }
        val messageText = localizedEmailMessageFactory.inactivity(language, companyName)
        val content = brandedEmailTemplateRenderer.render(
            title = messageText.title,
            intro = messageText.intro,
            actionLabel = messageText.actionLabel,
            actionUrl = "${brandedEmailTemplateRenderer.publicBaseUrl}/$normalizedLanguage/login",
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
