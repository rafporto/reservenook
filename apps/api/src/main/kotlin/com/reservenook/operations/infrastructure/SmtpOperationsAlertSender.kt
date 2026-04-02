package com.reservenook.operations.infrastructure

import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.operations.application.OperationsAlertMessage
import com.reservenook.operations.application.OperationsAlertProperties
import com.reservenook.operations.application.OperationsAlertSender
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

class SmtpOperationsAlertSender(
    private val mailSender: JavaMailSender,
    private val brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer,
    private val operationsAlertProperties: OperationsAlertProperties
) : OperationsAlertSender {

    override fun sendAlert(recipientEmail: String, message: OperationsAlertMessage) {
        val intro = buildString {
            append(message.intro)
            if (message.details.isNotEmpty()) {
                append("\n\n")
                append(message.details.entries.joinToString(separator = "\n") { (label, value) -> "$label: $value" })
            }
        }
        val content = brandedEmailTemplateRenderer.render(
            title = message.title,
            intro = intro,
            actionLabel = message.actionLabel,
            actionUrl = message.actionUrl,
            footerNote = message.footerNote
        )

        val mimeMessage: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(mimeMessage, "UTF-8")
        helper.setFrom(operationsAlertProperties.fromEmail)
        helper.setTo(recipientEmail)
        helper.setSubject(message.subject)
        helper.setText(content.plainText, content.html)
        mailSender.send(mimeMessage)
    }
}
