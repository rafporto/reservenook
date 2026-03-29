package com.reservenook.platformadmin.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.registration.domain.BusinessType
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyStatus
import com.reservenook.registration.domain.CompanySubscription
import com.reservenook.registration.domain.PlanType
import com.reservenook.registration.infrastructure.CompanyRepository
import com.reservenook.registration.infrastructure.CompanySubscriptionRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

class PlatformAdminCompanyListServiceTest {

    private val companyRepository = mockk<CompanyRepository>()
    private val companySubscriptionRepository = mockk<CompanySubscriptionRepository>()
    private val service = PlatformAdminCompanyListService(companyRepository, companySubscriptionRepository)

    @Test
    fun `platform admin receives company list with required summary fields`() {
        val company = Company(
            id = 1L,
            name = "Acme Wellness",
            businessType = BusinessType.APPOINTMENT,
            slug = "acme-wellness",
            status = CompanyStatus.ACTIVE,
            defaultLanguage = "en",
            defaultLocale = "en-US"
        )
        val subscription = CompanySubscription(
            id = 2L,
            company = company,
            planType = PlanType.TRIAL,
            startsAt = Instant.parse("2026-03-20T00:00:00Z"),
            expiresAt = Instant.parse("2026-03-27T00:00:00Z")
        )

        every { companyRepository.findAll() } returns listOf(company)
        every { companySubscriptionRepository.findAll() } returns listOf(subscription)

        val result = service.listCompanies(
            AppAuthenticatedUser(userId = 1L, email = "platform@reservenook.com", isPlatformAdmin = true)
        )

        result shouldHaveSize 1
        result.single().companyName shouldBe "Acme Wellness"
        result.single().activationStatus shouldBe "ACTIVE"
        result.single().planType shouldBe "TRIAL"
    }

    @Test
    fun `non platform user is denied`() {
        val exception = assertThrows<ResponseStatusException> {
            service.listCompanies(
                AppAuthenticatedUser(
                    userId = 1L,
                    email = "admin@acme.com",
                    isPlatformAdmin = false,
                    companySlug = "acme-wellness"
                )
            )
        }

        exception.statusCode.value() shouldBe 403
    }
}
