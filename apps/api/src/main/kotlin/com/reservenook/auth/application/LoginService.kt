package com.reservenook.auth.application

import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val userAccountRepository: UserAccountRepository,
    private val companyMembershipRepository: CompanyMembershipRepository,
    private val passwordEncoder: PasswordEncoder,
    private val securityContextRepository: HttpSessionSecurityContextRepository
) {

    fun login(
        email: String,
        password: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): LoginResult {
        val normalizedEmail = email.trim().lowercase()
        val user = userAccountRepository.findByEmail(normalizedEmail)
            ?: throw LoginFailedException("Invalid email or password.", LoginFailureCode.INVALID_CREDENTIALS)

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw LoginFailedException("Invalid email or password.", LoginFailureCode.INVALID_CREDENTIALS)
        }

        if (!user.emailVerified || user.status != UserStatus.ACTIVE) {
            throw LoginFailedException(
                "Your account is not active yet. Request a new activation email.",
                LoginFailureCode.ACTIVATION_REQUIRED
            )
        }

        val loginResult = if (user.isPlatformAdmin) {
            LoginResult(
                authenticatedUser = AppAuthenticatedUser(
                    userId = requireNotNull(user.id),
                    email = user.email,
                    isPlatformAdmin = true
                ),
                redirectTo = "/platform-admin"
            )
        } else {
            val membership = companyMembershipRepository.findFirstByUserId(requireNotNull(user.id))
                ?: throw LoginFailedException("Invalid email or password.", LoginFailureCode.INVALID_CREDENTIALS)

            if (membership.company.status != CompanyStatus.ACTIVE) {
                throw LoginFailedException(
                    "The company account is not active.",
                    LoginFailureCode.INACTIVE_COMPANY
                )
            }

            LoginResult(
                authenticatedUser = AppAuthenticatedUser(
                    userId = requireNotNull(user.id),
                    email = user.email,
                    isPlatformAdmin = false,
                    companySlug = membership.company.slug
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

        return loginResult
    }
}
