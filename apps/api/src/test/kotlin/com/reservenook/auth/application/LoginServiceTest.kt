package com.reservenook.auth.application

import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.RequestThrottleService
import com.reservenook.security.application.SecurityAuditService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository

class LoginServiceTest {

    private val userAccountRepository = mockk<UserAccountRepository>()
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()
    private val securityContextRepository = mockk<HttpSessionSecurityContextRepository>(relaxed = true)
    private val requestThrottleService = mockk<RequestThrottleService>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)

    private val service = LoginService(
        userAccountRepository = userAccountRepository,
        companyMembershipRepository = companyMembershipRepository,
        passwordEncoder = passwordEncoder,
        securityContextRepository = securityContextRepository,
        requestThrottleService = requestThrottleService,
        securityAuditService = securityAuditService
    )

    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)

    @Test
    fun `login returns company redirect for active company admin`() {
        val user = activeCompanyUser()
        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { passwordEncoder.matches("SecurePass123", "encoded") } returns true
        every { companyMembershipRepository.findFirstByUserId(2L) } returns activeMembership(user)

        val result = service.login("admin@acme.com", "SecurePass123", request, response)

        result.redirectTo shouldBe "/app/company/acme-wellness"
        result.authenticatedUser.isPlatformAdmin shouldBe false
    }

    @Test
    fun `login returns platform admin redirect`() {
        val user = UserAccount(
            id = 10L,
            email = "platform@reservenook.com",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true,
            isPlatformAdmin = true
        )
        every { userAccountRepository.findByEmail("platform@reservenook.com") } returns user
        every { passwordEncoder.matches("SecurePass123", "encoded") } returns true

        val result = service.login("platform@reservenook.com", "SecurePass123", request, response)

        result.redirectTo shouldBe "/platform-admin"
        result.authenticatedUser.isPlatformAdmin shouldBe true
    }

    @Test
    fun `login rejects invalid credentials`() {
        every { userAccountRepository.findByEmail("admin@acme.com") } returns activeCompanyUser()
        every { passwordEncoder.matches("wrong-pass", "encoded") } returns false

        val exception = org.junit.jupiter.api.assertThrows<LoginFailedException> {
            service.login("admin@acme.com", "wrong-pass", request, response)
        }

        exception.code shouldBe LoginFailureCode.INVALID_CREDENTIALS
    }

    @Test
    fun `login rejects unverified account`() {
        val user = UserAccount(
            id = 2L,
            email = "admin@acme.com",
            passwordHash = "encoded",
            status = UserStatus.PENDING_ACTIVATION,
            emailVerified = false
        )
        every { userAccountRepository.findByEmail("admin@acme.com") } returns user
        every { passwordEncoder.matches("SecurePass123", "encoded") } returns true

        val exception = org.junit.jupiter.api.assertThrows<LoginFailedException> {
            service.login("admin@acme.com", "SecurePass123", request, response)
        }

        exception.code shouldBe LoginFailureCode.ACTIVATION_REQUIRED
    }

    private fun activeCompanyUser() = UserAccount(
        id = 2L,
        email = "admin@acme.com",
        passwordHash = "encoded",
        status = UserStatus.ACTIVE,
        emailVerified = true
    )

    private fun activeMembership(user: UserAccount) = CompanyMembership(
        id = 5L,
        company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        ),
        user = user,
        role = CompanyRole.COMPANY_ADMIN
    )
}
