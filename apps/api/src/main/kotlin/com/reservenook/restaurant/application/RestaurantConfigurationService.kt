package com.reservenook.restaurant.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.CompanyBackofficeDiningAreaSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantFloorbookEntrySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantReservationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantServicePeriodSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableCombinationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RestaurantConfigurationService(
    private val diningAreaManagementService: DiningAreaManagementService,
    private val restaurantTableManagementService: RestaurantTableManagementService,
    private val tableCombinationManagementService: TableCombinationManagementService,
    private val restaurantServicePeriodManagementService: RestaurantServicePeriodManagementService,
    private val publicRestaurantAvailabilityService: PublicRestaurantAvailabilityService,
    private val publicRestaurantReservationService: PublicRestaurantReservationService,
    private val restaurantReservationOutcomeService: RestaurantReservationOutcomeService,
    private val restaurantFloorbookService: RestaurantFloorbookService
) {
    @Transactional(readOnly = true)
    fun listDiningAreas(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeDiningAreaSummary> =
        diningAreaManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertDiningArea(principal: AppAuthenticatedUser, requestedSlug: String, diningAreaId: Long?, name: String, displayOrder: Int, active: Boolean) =
        diningAreaManagementService.upsert(principal, requestedSlug, diningAreaId, name, displayOrder, active)

    @Transactional(readOnly = true)
    fun listTables(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantTableSummary> =
        restaurantTableManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertTable(principal: AppAuthenticatedUser, requestedSlug: String, tableId: Long?, diningAreaId: Long, label: String, minPartySize: Int, maxPartySize: Int, active: Boolean) =
        restaurantTableManagementService.upsert(principal, requestedSlug, tableId, diningAreaId, label, minPartySize, maxPartySize, active)

    @Transactional(readOnly = true)
    fun listCombinations(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantTableCombinationSummary> =
        tableCombinationManagementService.list(principal, requestedSlug)

    @Transactional
    fun replaceCombinations(principal: AppAuthenticatedUser, requestedSlug: String, entries: List<TableCombinationDraft>) =
        tableCombinationManagementService.replace(principal, requestedSlug, entries)

    @Transactional(readOnly = true)
    fun listServicePeriods(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantServicePeriodSummary> =
        restaurantServicePeriodManagementService.list(principal, requestedSlug)

    @Transactional
    fun upsertServicePeriod(principal: AppAuthenticatedUser, requestedSlug: String, servicePeriodId: Long?, name: String, dayOfWeek: String, opensAt: String, closesAt: String, slotIntervalMinutes: Int, reservationDurationMinutes: Int, minPartySize: Int, maxPartySize: Int, bookingWindowDays: Int, active: Boolean) =
        restaurantServicePeriodManagementService.upsert(principal, requestedSlug, servicePeriodId, name, dayOfWeek, opensAt, closesAt, slotIntervalMinutes, reservationDurationMinutes, minPartySize, maxPartySize, bookingWindowDays, active)

    @Transactional(readOnly = true)
    fun getPublicAvailability(slug: String, date: String, partySize: Int, clientAddress: String) =
        publicRestaurantAvailabilityService.getAvailability(slug, date, partySize, clientAddress)

    @Transactional
    fun bookPublicReservation(slug: String, clientAddress: String, fullName: String, email: String, phone: String?, preferredLanguage: String?, partySize: Int, startsAt: String): CompanyBackofficeRestaurantReservationSummary =
        publicRestaurantReservationService.book(slug, clientAddress, fullName, email, phone, preferredLanguage, partySize, startsAt)

    @Transactional(readOnly = true)
    fun listReservations(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeRestaurantReservationSummary> =
        restaurantReservationOutcomeService.list(principal, requestedSlug)

    @Transactional
    fun updateReservation(principal: AppAuthenticatedUser, requestedSlug: String, reservationId: Long, status: String, tableIds: List<Long>?) =
        restaurantReservationOutcomeService.update(principal, requestedSlug, reservationId, status, tableIds)

    @Transactional(readOnly = true)
    fun floorbook(principal: AppAuthenticatedUser, requestedSlug: String, date: String): List<CompanyBackofficeRestaurantFloorbookEntrySummary> =
        restaurantFloorbookService.list(principal, requestedSlug, date)
}
