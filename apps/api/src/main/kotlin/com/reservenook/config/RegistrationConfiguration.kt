package com.reservenook.config

import com.reservenook.operations.application.OperationsAlertProperties
import com.reservenook.registration.application.RegistrationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableConfigurationProperties(RegistrationProperties::class, OperationsAlertProperties::class)
class RegistrationConfiguration {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
