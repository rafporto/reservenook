package com.reservenook.operations.infrastructure

import com.reservenook.config.BrandedEmailTemplateRenderer
import com.reservenook.operations.application.OperationsAlertProperties
import com.reservenook.operations.application.OperationsAlertSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

@Configuration
class OperationsAlertConfiguration {

    @Bean
    @ConditionalOnBean(JavaMailSender::class)
    fun smtpOperationsAlertSender(
        mailSender: JavaMailSender,
        brandedEmailTemplateRenderer: BrandedEmailTemplateRenderer,
        operationsAlertProperties: OperationsAlertProperties
    ): OperationsAlertSender =
        SmtpOperationsAlertSender(mailSender, brandedEmailTemplateRenderer, operationsAlertProperties)

    @Bean
    @ConditionalOnMissingBean(OperationsAlertSender::class)
    fun noopOperationsAlertSender(): OperationsAlertSender = NoopOperationsAlertSender()
}
