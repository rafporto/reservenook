package com.reservenook.companybackoffice.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership
import com.reservenook.registration.domain.CompanyRole
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.UserAccount
import com.reservenook.registration.domain.UserStatus
import com.reservenook.registration.infrastructure.CompanyMembershipRepository
import com.reservenook.security.application.SecurityAuditService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.web.server.ResponseStatusException

class CompanyProfileServiceTest {

    private val companyMembershipRepository = mockk<CompanyMembershipRepository>()
    private val securityAuditService = mockk<SecurityAuditService>(relaxed = true)
    private val service = CompanyProfileService(companyMembershipRepository, securityAuditService)

    @Test
    fun `updates company profile for authorized company admin`() {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val membership = CompanyMembership(
            company = company,
            user = UserAccount(
                id = 2L,
                email = "admin@acme.com",
                passwordHash = "hashed",
                status = UserStatus.ACTIVE,
                emailVerified = true
            ),
            role = CompanyRole.COMPANY_ADMIN
        )

        every {
            companyMembershipRepository.findFirstByUserEmailAndCompanySlug("admin@acme.com", "acme-wellness")
        } returns membership

        val result = service.updateProfile(
            principal = AppAuthenticatedUser(
                userId = 2L,
                email = "admin@acme.com",
                isPlatformAdmin = false,
                companySlug = "acme-wellness"
            ),
            requestedSlug = "acme-wellness",
            companyName = "Acme Wellness Studio",
            businessDescription = "Premium appointments and classes.",
            contactEmail = "hello@acme.com",
            contactPhone = "+49 30 555 0000",
            addressLine1 = "Alexanderplatz 1",
            addressLine2 = "Floor 3",
            city = "Berlin",
            postalCode = "10178",
            countryCode = "de"
        )

        assertEquals("Acme Wellness Studio", result.company.companyName)
        assertEquals("hello@acme.com", result.profile.contactEmail)
        assertEquals("DE", result.profile.countryCode)
    }

    @Test
    fun `rejects invalid contact email`() {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val membership = CompanyMembership(
            company = company,
            user = UserAccount(
                id = 2L,
                email = "admin@acme.com",
                passwordHash = "hashed",
                status = UserStatus.ACTIVE,
                emailVerified = true
            ),
            role = CompanyRole.COMPANY_ADMIN
        )

        every {
            companyMembershipRepository.findFirstByUserEmailAndCompanySlug("admin@acme.com", "acme-wellness")
        } returns membership

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.updateProfile(
                principal = AppAuthenticatedUser(
                    userId = 2L,
                    email = "admin@acme.com",
                    isPlatformAdmin = false,
                    companySlug = "acme-wellness"
                ),
                requestedSlug = "acme-wellness",
                companyName = "Acme Wellness Studio",
                businessDescription = null,
                contactEmail = "invalid-email",
                contactPhone = "+49 30 555 0000",
                addressLine1 = "Alexanderplatz 1",
                addressLine2 = null,
                city = "Berlin",
                postalCode = "10178",
                countryCode = "DE"
            )
        }

        assertEquals("Contact email must be a valid email address.", exception.message)
    }

    @Test
    fun `rejects unauthorized tenant access`() {
        every {
            companyMembershipRepository.findFirstByUserEmailAndCompanySlug("admin@acme.com", "other-company")
        } returns null

        assertThrows(ResponseStatusException::class.java) {
            service.updateProfile(
                principal = AppAuthenticatedUser(
                    userId = 2L,
                    email = "admin@acme.com",
                    isPlatformAdmin = false,
                    companySlug = "acme-wellness"
                ),
                requestedSlug = "other-company",
                companyName = "Acme Wellness Studio",
                businessDescription = null,
                contactEmail = "hello@acme.com",
                contactPhone = "+49 30 555 0000",
                addressLine1 = "Alexanderplatz 1",
                addressLine2 = null,
                city = "Berlin",
                postalCode = "10178",
                countryCode = "DE"
            )
        }
    }
}
