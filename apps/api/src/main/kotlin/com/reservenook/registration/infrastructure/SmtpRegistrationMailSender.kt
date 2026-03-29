package com.reservenook.registration.infrastructure

import com.reservenook.registration.application.RegistrationMailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class SmtpRegistrationMailSender(
    private val mailSender: JavaMailSender
) : RegistrationMailSender {

    override fun sendActivationEmail(email: String, activationLink: String) {
        val message = SimpleMailMessage()
        message.setTo(email)
        message.subject = "Activate your Reservenook account"
        message.text = "Activate your company account using this link: $activationLink"
        mailSender.send(message)
    }
}
