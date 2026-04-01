package com.reservenook.auth.application

import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.RequestFingerprintResolver
import com.reservenook.security.application.RequestThrottleService
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.application.TooManyRequestsException
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class LoginService(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val passwordEncoder: PasswordEncoder,
    private val securityContextRepository: HttpSessionSecurityContextRepository,
    private val requestThrottleService: RequestThrottleService,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional
    fun login(
        email: String,
        password: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): LoginResult {
        val normalizedEmail = email.trim().lowercase()
        val requestFingerprint = RequestFingerprintResolver.resolve(request, normalizedEmail)
        try {
            requestThrottleService.assertAllowed("login", requestFingerprint, 5, Duration.ofMinutes(10))
        } catch (exception: TooManyRequestsException) {
            securityAuditService.record(
                eventType = SecurityAuditEventType.LOGIN_RATE_LIMITED,
                outcome = SecurityAuditOutcome.RATE_LIMITED,
                targetEmail = normalizedEmail
            )
            throw exception
        }
        val user = userAccountRepository.findByEmail(normalizedEmail)
            ?: failLogin(normalizedEmail, LoginFailureCode.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            failLogin(normalizedEmail, LoginFailureCode.INVALID_CREDENTIALS)
        }

        if (!user.emailVerified || user.status != UserStatus.ACTIVE) {
            failLogin(normalizedEmail, LoginFailureCode.ACTIVATION_REQUIRED)
        }

        val loginResult = if (user.isPlatformAdmin) {
            LoginResult(
                authenticatedUser = AppAuthenticatedUser(
                    userId = requireNotNull(user.id),
                    email = user.email,
                    isPlatformAdmin = true,
                    passwordVersion = user.passwordVersion
                ),
                redirectTo = "/platform-admin"
            )
        } else {
            val membership = companyMembershipRepository.findFirstByUserId(requireNotNull(user.id))
                ?: failLogin(normalizedEmail, LoginFailureCode.INVALID_CREDENTIALS)

            if (membership.company.status != CompanyStatus.ACTIVE) {
                failLogin(normalizedEmail, LoginFailureCode.INACTIVE_COMPANY)
            }

            membership.company.lastActivityAt = Instant.now()

            LoginResult(
                authenticatedUser = AppAuthenticatedUser(
                    userId = requireNotNull(user.id),
                    email = user.email,
                    isPlatformAdmin = false,
                    companySlug = membership.company.slug,
                    passwordVersion = user.passwordVersion
                ),
                redirectTo = "/app/company/${membership.company.slug}"
            )
        }

        val authority = if (loginResult.authenticatedUser.isPlatformAdmin) {
            SimpleGrantedAuthority("ROLE_PLATFORM_ADMIN")
        } else {
            SimpleGrantedAuthority("ROLE_COMPANY_ADMIN")
        }

        val authentication = UsernamePasswordAuthenticationToken(
            loginResult.authenticatedUser,
            null,
            listOf(authority)
        )

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
        securityContextRepository.saveContext(context, request, response)
        requestThrottleService.clear("login", requestFingerprint)

        return loginResult
    }

    private fun failLogin(email: String, code: LoginFailureCode): Nothing {
        securityAuditService.record(
            eventType = SecurityAuditEventType.LOGIN_FAILURE,
            outcome = SecurityAuditOutcome.FAILURE,
            targetEmail = email,
            details = code.name
        )
        throw LoginFailedException("Invalid email or password.", code)
    }
}
