package com.reservenook.config

import com.reservenook.security.application.SessionCredentialVersionFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy
import org.springframework.security.web.header.writers.StaticHeadersWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfiguration(
    @Value("\${NEXT_PUBLIC_APP_URL:http://localhost:3000}")
    private val frontendOrigin: String,
    private val sessionCredentialVersionFilter: SessionCredentialVersionFilter
) {

    private val apiContentSecurityPolicy =
        "default-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'"

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf {
                it.ignoringRequestMatchers("/actuator/health", "/api/public/**")
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .headers {
                it.frameOptions { frameOptions -> frameOptions.deny() }
                    .contentTypeOptions(Customizer.withDefaults())
                    .referrerPolicy { referrerPolicy ->
                        referrerPolicy.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
                    .contentSecurityPolicy { contentSecurityPolicy ->
                        contentSecurityPolicy.policyDirectives(apiContentSecurityPolicy)
                    }
                    .addHeaderWriter(StaticHeadersWriter("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
            }
            .authorizeHttpRequests {
                it.requestMatchers("/actuator/health", "/api/public/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterAfter(sessionCredentialVersionFilter, AnonymousAuthenticationFilter::class.java)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(frontendOrigin)
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityContextRepository(): HttpSessionSecurityContextRepository = HttpSessionSecurityContextRepository()
}
