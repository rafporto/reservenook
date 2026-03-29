package com.reservenook.auth.infrastructure

import com.reservenook.auth.application.PasswordResetMailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class SmtpPasswordResetMailSender(
    private val mailSender: JavaMailSender
) : PasswordResetMailSender {

    override fun sendPasswordResetEmail(email: String, resetLink: String) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "Reset your Reservenook password"
        message.text = "Reset your password using this link: $resetLink"
        mailSender.send(message)
    }
}
