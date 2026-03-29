package com.reservenook.registration.application

import com.reservenook.registration.domain.ActivationToken
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.CompanySubscription
import com.reservenook.registration.domain.PlanType
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.ActivationTokenRepository
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CompanyRegistrationService(
    private val companyRepository: CompanyRepository,
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val companySubscriptionRepository: CompanySubscriptionRepository,
    private val activationTokenRepository: ActivationTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val registrationMailSender: RegistrationMailSender,
    private val registrationProperties: RegistrationProperties
) {

    @Transactional
    fun register(command: RegisterCompanyCommand): RegisterCompanyResult {
        val normalizedSlug = command.slug.trim().lowercase()
        val normalizedEmail = command.email.trim().lowercase()

        if (companyRepository.existsBySlug(normalizedSlug)) {
            throw RegistrationConflictException("Company slug is already in use.")
        }

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            throw RegistrationConflictException("Email address is already in use.")
        }

        if (command.password.length < 8) {
            throw IllegalArgumentException("Password must contain at least 8 characters.")
        }

        if (command.defaultLanguage.lowercase() !in setOf("en", "de", "pt")) {
            throw IllegalArgumentException("Default language is not supported.")
        }

        val now = Instant.now()
        val company = companyRepository.save(
            Company(
                name = command.companyName.trim(),
                businessType = command.businessType,
                slug = normalizedSlug,
                status = CompanyStatus.PENDING_ACTIVATION,
                defaultLanguage = command.defaultLanguage.lowercase(),
                defaultLocale = command.defaultLocale
            )
        )

        val user = userAccountRepository.save(
            UserAccount(
                email = normalizedEmail,
                passwordHash = passwordEncoder.encode(command.password),
                status = UserStatus.PENDING_ACTIVATION,
                emailVerified = false
            )
        )

        companyMembershipRepository.save(
            CompanyMembership(
                company = company,
                user = user,
                role = CompanyRole.COMPANY_ADMIN
            )
        )

        companySubscriptionRepository.save(
            CompanySubscription(
                company = company,
                planType = command.planType,
                startsAt = now,
                expiresAt = when (command.planType) {
                    PlanType.TRIAL -> now.plus(7, ChronoUnit.DAYS)
                    PlanType.PAID -> now.plus(365, ChronoUnit.DAYS)
                }
            )
        )

        val activationToken = activationTokenRepository.save(
            ActivationToken(
                token = UUID.randomUUID().toString(),
                company = company,
                user = user,
                expiresAt = now.plus(registrationProperties.activationTokenHours, ChronoUnit.HOURS)
            )
        )

        registrationMailSender.sendActivationEmail(
            normalizedEmail,
            "${registrationProperties.publicBaseUrl.trimEnd('/')}/${company.defaultLanguage}/activate?token=${activationToken.token}"
        )

        return RegisterCompanyResult(
            companyId = requireNotNull(company.id),
            activationToken = activationToken.token
        )
    }
}
