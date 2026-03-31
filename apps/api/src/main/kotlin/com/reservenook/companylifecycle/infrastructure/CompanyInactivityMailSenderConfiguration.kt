package com.reservenook.companylifecycle.infrastructure

import com.reservenook.companylifecycle.application.CompanyDeletionWarningMailSender
import com.reservenook.companylifecycle.application.CompanyInactivityMailSender
import com.reservenook.config.BrandedEmailTemplateRenderer
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
class CompanyInactivityMailSenderConfiguration {

    @Bean
    @ConditionalOnBean(JavaMailSender::class)
    fun smtpCompanyInactivityMailSender(
        mailSender: JavaMailSender,
        brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
    ): CompanyInactivityMailSender =
        SmtpCompanyInactivityMailSender(mailSender, brandedEmailTemplateRenderer)

    @Bean
    @ConditionalOnMissingBean(CompanyInactivityMailSender::class)
    fun noopCompanyInactivityMailSender(): CompanyInactivityMailSender = NoopCompanyInactivityMailSender()

    @Bean
    @ConditionalOnBean(JavaMailSender::class)
    fun smtpCompanyDeletionWarningMailSender(
        mailSender: JavaMailSender,
        brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer
    ): CompanyDeletionWarningMailSender =
        SmtpCompanyDeletionWarningMailSender(mailSender, brandedEmailTemplateRenderer)

    @Bean
    @ConditionalOnMissingBean(CompanyDeletionWarningMailSender::class)
    fun noopCompanyDeletionWarningMailSender(): CompanyDeletionWarningMailSender = NoopCompanyDeletionWarningMailSender()
}
