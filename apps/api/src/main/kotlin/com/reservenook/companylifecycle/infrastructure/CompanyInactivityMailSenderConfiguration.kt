package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyInactivityMailSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
class CompanyInactivityMailSenderConfiguration {

    @Bean
    @ConditionalOnBean(JavaMailSender::class)
    fun smtpCompanyInactivityMailSender(mailSender: JavaMailSender): CompanyInactivityMailSender =
        SmtpCompanyInactivityMailSender(mailSender)

    @Bean
    @ConditionalOnMissingBean(CompanyInactivityMailSender::class)
    fun noopCompanyInactivityMailSender(): CompanyInactivityMailSender = NoopCompanyInactivityMailSender()
}
