package com.reservenook.auth.application

interface PasswordResetMailSender {
    fun sendPasswordResetEmail(email: String, resetLink: String, language: String)
}
