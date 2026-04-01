package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.auth.infrastructure.PasswordResetTokenRepository
import com.reservenook.companybackoffice.infrastructure.CompanyBusinessHourRepository
import com.reservenook.companybackoffice.infrastructure.CompanyClosureDateRepository
import com.reservenook.companybackoffice.infrastructure.CompanyCustomerQuestionRepository
import com.reservenook.registration.application.RegistrationProperties
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.registration.infrastructure.UserAccountRepository
import com.reservenook.security.application.SecurityAuditService
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class CompanyConfigurationServiceTest {

    private val companyAdminAccessService = mockk<CompanyAdminAccessService>()
    private val companyMembershipRepository = mockk<CompanyMembershipRepository>(relaxed = true)
    private val userAccountRepository = mockk<UserAccountRepository>(relaxed = true)
    private val companyBusinessHourRepository = mockk<CompanyBusinessHourRepository>(relaxed = true)
    private val companyClosureDateRepository = mockk<CompanyClosureDateRepository>(relaxed = true)
    private val companyCustomerQuestionRepository = mockk<CompanyCustomerQuestionRepository>(relaxed = true)
    private val passwordResetTokenRepository = mockk<PasswordResetTokenRepository>(relaxed = true)
    private val passwordResetMailSender = mockk<PasswordResetMailSender>(relaxed = true)
    private val passwordEncoder = mockk<PasswordEncoder>(relaxed = true)
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val registrationProperties = RegistrationProperties(
        publicBaseUrl = "http://localhost:3000",
        activationTokenHours = 48,
        resendCooldownMinutes = 5,
        passwordResetTokenHours = 2,
        passwordResetCooldownMinutes = 5
    )

    private val service = CompanyConfigurationService(
        companyAdminAccessService = companyAdminAccessService,
        companyMembershipRepository = companyMembershipRepository,
        userAccountRepository = userAccountRepository,
        companyBusinessHourRepository = companyBusinessHourRepository,
        companyClosureDateRepository = companyClosureDateRepository,
        companyCustomerQuestionRepository = companyCustomerQuestionRepository,
        passwordResetTokenRepository = passwordResetTokenRepository,
        passwordResetMailSender = passwordResetMailSender,
        passwordEncoder = passwordEncoder,
        registrationProperties = registrationProperties,
        securityAuditService = securityAuditService
    )

    @Test
    fun `update localization rejects incompatible locale`() {
        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns adminMembership()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateLocalization(
                principal = principal(),
                requestedSlug = "acme-wellness",
                defaultLanguage = "de",
                defaultLocale = "en-US"
            )
        }

        assertEquals("Default locale is not compatible with the selected language.", exception.message)
    }

    @Test
    fun `update business hours rejects overlapping windows`() {
        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns adminMembership()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateBusinessHours(
                principal = principal(),
                requestedSlug = "acme-wellness",
                entries = listOf(
                    BusinessHourDraft("MONDAY", "09:00", "12:00", 0),
                    BusinessHourDraft("MONDAY", "11:00", "15:00", 1)
                )
            )
        }

        assertEquals("Business hour windows cannot overlap on the same day.", exception.message)
    }

    @Test
    fun `update closure dates rejects end date before start date`() {
        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns adminMembership()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateClosureDates(
                principal = principal(),
                requestedSlug = "acme-wellness",
                entries = listOf(ClosureDateDraft("Holiday", "2026-12-24", "2026-12-20"))
            )
        }

        assertEquals("Closure end date must be on or after the start date.", exception.message)
    }

    @Test
    fun `create staff user provisions membership and sends invitation`() {
        val membership = adminMembership()
        val userSlot = slot<UserAccount>()
        val membershipSlot = slot<CompanyMembership>()

        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns membership
        every { userAccountRepository.existsByEmail("staff@acme.com") } returns false
        every { passwordEncoder.encode(any()) } returns "encoded"
        every { userAccountRepository.save(capture(userSlot)) } answers {
            userSlot.captured.id = 10L
            userSlot.captured
        }
        every { companyMembershipRepository.save(capture(membershipSlot)) } answers {
            membershipSlot.captured.id = 20L
            membershipSlot.captured
        }
        every { passwordResetTokenRepository.save(any()) } answers { firstArg() }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }

        val result = service.createStaffUser(
            principal = principal(),
            requestedSlug = "acme-wellness",
            fullName = "Support Agent",
            email = "staff@acme.com",
            role = "STAFF"
        )

        assertEquals("Support Agent", result.fullName)
        assertEquals("STAFF", result.role)
        verify(exactly = 1) {
            passwordResetMailSender.sendPasswordResetEmail(
                "staff@acme.com",
                match { it.startsWith("http://localhost:3000/en/reset-password?token=") },
                "en"
            )
        }
    }

    @Test
    fun `update staff user rejects removal of the last admin`() {
        val membership = adminMembership()
        val targetMembership = adminMembership()
        targetMembership.id = 30L

        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns membership
        every { companyMembershipRepository.findByIdAndCompanyId(30L, 1L) } returns targetMembership
        every {
            companyMembershipRepository.countByCompanyIdAndRoleAndUserStatus(
                1L,
                CompanyRole.COMPANY_ADMIN,
                UserStatus.ACTIVE
            )
        } returns 1L

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateStaffUser(
                principal = principal(),
                requestedSlug = "acme-wellness",
                membershipId = 30L,
                role = "STAFF",
                status = "ACTIVE"
            )
        }

        assertEquals("You cannot remove your own active company-admin access.", exception.message)
    }

    @Test
    fun `update staff user rejects losing the last active admin when another admin is already inactive`() {
        val actingMembership = adminMembership()
        val targetMembership = CompanyMembership(
            id = 30L,
            company = actingMembership.company,
            user = UserAccount(
                id = 7L,
                email = "other-admin@acme.com",
                fullName = "Other Admin",
                passwordHash = "encoded",
                status = UserStatus.ACTIVE,
                emailVerified = true
            ),
            role = CompanyRole.COMPANY_ADMIN
        )

        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns actingMembership
        every { companyMembershipRepository.findByIdAndCompanyId(30L, 1L) } returns targetMembership
        every {
            companyMembershipRepository.countByCompanyIdAndRoleAndUserStatus(
                1L,
                CompanyRole.COMPANY_ADMIN,
                UserStatus.ACTIVE
            )
        } returns 1L

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateStaffUser(
                principal = principal(),
                requestedSlug = "acme-wellness",
                membershipId = 30L,
                role = "STAFF",
                status = "ACTIVE"
            )
        }

        assertEquals("At least one company admin must remain active.", exception.message)
    }

    @Test
    fun `update customer questions rejects select questions without options`() {
        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns adminMembership()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateCustomerQuestions(
                principal = principal(),
                requestedSlug = "acme-wellness",
                entries = listOf(
                    CustomerQuestionDraft(
                        label = "Preferred provider",
                        questionType = "SINGLE_SELECT",
                        required = true,
                        enabled = true,
                        displayOrder = 0,
                        options = emptyList()
                    )
                )
            )
        }

        assertEquals("Selectable questions require at least two options.", exception.message)
    }

    @Test
    fun `update widget settings rejects invalid host names`() {
        every { companyAdminAccessService.requireCompanyAdmin(any(), "acme-wellness") } returns adminMembership()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateWidgetSettings(
                principal = principal(),
                requestedSlug = "acme-wellness",
                ctaLabel = "Reserve now",
                widgetEnabled = true,
                allowedDomains = listOf("https://bad.example.com"),
                themeVariant = "minimal"
            )
        }

        assertEquals("Allowed widget domains must be valid host names.", exception.message)
    }

    private fun principal() = com.reservenook.auth.application.AppAuthenticatedUser(
        userId = 2L,
        email = "admin@acme.com",
        isPlatformAdmin = false,
        companySlug = "acme-wellness",
        companyRole = CompanyRole.COMPANY_ADMIN.name
    )

    private fun adminMembership(): CompanyMembership = CompanyMembership(
        id = 11L,
        company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        ),
        user = UserAccount(
            id = 2L,
            email = "admin@acme.com",
            fullName = "Admin User",
            passwordHash = "encoded",
            status = UserStatus.ACTIVE,
            emailVerified = true
        ),
        role = CompanyRole.COMPANY_ADMIN
    )
}
