package com.reservenook.registration.application

interface RegistrationMailSender {
    fun sendActivationEmail(email: String, activationLink: String)
}
