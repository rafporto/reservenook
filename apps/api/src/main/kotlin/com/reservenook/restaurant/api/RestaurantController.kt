package com.reservenook.restaurant.api

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.companybackoffice.api.DiningAreasResponse
import com.reservenook.companybackoffice.api.ReplaceRestaurantTableCombinationsRequest
import com.reservenook.companybackoffice.api.ReplaceRestaurantTableCombinationsResponse
import com.reservenook.companybackoffice.api.RestaurantFloorbookResponse
import com.reservenook.companybackoffice.api.RestaurantReservationsResponse
import com.reservenook.companybackoffice.api.RestaurantServicePeriodsResponse
import com.reservenook.companybackoffice.api.RestaurantTableCombinationsResponse
import com.reservenook.companybackoffice.api.RestaurantTablesResponse
import com.reservenook.companybackoffice.api.UpdateRestaurantReservationOutcomeRequest
import com.reservenook.companybackoffice.api.UpdateRestaurantReservationOutcomeResponse
import com.reservenook.companybackoffice.api.UpsertDiningAreaRequest
import com.reservenook.companybackoffice.api.UpsertDiningAreaResponse
import com.reservenook.companybackoffice.api.UpsertRestaurantServicePeriodRequest
import com.reservenook.companybackoffice.api.UpsertRestaurantServicePeriodResponse
import com.reservenook.companybackoffice.api.UpsertRestaurantTableRequest
import com.reservenook.companybackoffice.api.UpsertRestaurantTableResponse
import com.reservenook.restaurant.application.RestaurantConfigurationService
import com.reservenook.restaurant.application.TableCombinationDraft
import com.reservenook.security.application.RecentAuthenticationGuard
import jakarta.servlet.http.HttpSession
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RestaurantController(
    private val restaurantConfigurationService: RestaurantConfigurationService,
    private val recentAuthenticationGuard: RecentAuthenticationGuard
) {
    @GetMapping("/api/app/company/{slug}/dining-areas")
    fun listDiningAreas(@PathVariable slug: String, @AuthenticationPrincipal principal: AppAuthenticatedUser) =
        DiningAreasResponse(restaurantConfigurationService.listDiningAreas(principal, slug))

    @PostMapping("/api/app/company/{slug}/dining-areas")
    fun createDiningArea(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertDiningAreaRequest
    ) = UpsertDiningAreaResponse(
        message = "Dining area saved.",
        diningArea = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertDiningArea(principal, slug, null, request.name, request.displayOrder, request.active)
        }
    )

    @PutMapping("/api/app/company/{slug}/dining-areas/{diningAreaId}")
    fun updateDiningArea(
        @PathVariable slug: String,
        @PathVariable diningAreaId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertDiningAreaRequest
    ) = UpsertDiningAreaResponse(
        message = "Dining area updated.",
        diningArea = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertDiningArea(principal, slug, diningAreaId, request.name, request.displayOrder, request.active)
        }
    )

    @GetMapping("/api/app/company/{slug}/restaurant-tables")
    fun listTables(@PathVariable slug: String, @AuthenticationPrincipal principal: AppAuthenticatedUser) =
        RestaurantTablesResponse(restaurantConfigurationService.listTables(principal, slug))

    @PostMapping("/api/app/company/{slug}/restaurant-tables")
    fun createTable(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertRestaurantTableRequest
    ) = UpsertRestaurantTableResponse(
        message = "Restaurant table saved.",
        restaurantTable = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertTable(principal, slug, null, request.diningAreaId, request.label, request.minPartySize, request.maxPartySize, request.active)
        }
    )

    @PutMapping("/api/app/company/{slug}/restaurant-tables/{tableId}")
    fun updateTable(
        @PathVariable slug: String,
        @PathVariable tableId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertRestaurantTableRequest
    ) = UpsertRestaurantTableResponse(
        message = "Restaurant table updated.",
        restaurantTable = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertTable(principal, slug, tableId, request.diningAreaId, request.label, request.minPartySize, request.maxPartySize, request.active)
        }
    )

    @GetMapping("/api/app/company/{slug}/restaurant-table-combinations")
    fun listCombinations(@PathVariable slug: String, @AuthenticationPrincipal principal: AppAuthenticatedUser) =
        RestaurantTableCombinationsResponse(restaurantConfigurationService.listCombinations(principal, slug))

    @PutMapping("/api/app/company/{slug}/restaurant-table-combinations")
    fun replaceCombinations(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: ReplaceRestaurantTableCombinationsRequest
    ) = ReplaceRestaurantTableCombinationsResponse(
        message = "Restaurant table combinations updated.",
        restaurantTableCombinations = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.replaceCombinations(principal, slug, request.entries.map { TableCombinationDraft(it.primaryTableId, it.secondaryTableId) })
        }
    )

    @GetMapping("/api/app/company/{slug}/restaurant-service-periods")
    fun listServicePeriods(@PathVariable slug: String, @AuthenticationPrincipal principal: AppAuthenticatedUser) =
        RestaurantServicePeriodsResponse(restaurantConfigurationService.listServicePeriods(principal, slug))

    @PostMapping("/api/app/company/{slug}/restaurant-service-periods")
    fun createServicePeriod(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertRestaurantServicePeriodRequest
    ) = UpsertRestaurantServicePeriodResponse(
        message = "Restaurant service period saved.",
        restaurantServicePeriod = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertServicePeriod(principal, slug, null, request.name, request.dayOfWeek, request.opensAt, request.closesAt, request.slotIntervalMinutes, request.reservationDurationMinutes, request.minPartySize, request.maxPartySize, request.bookingWindowDays, request.active)
        }
    )

    @PutMapping("/api/app/company/{slug}/restaurant-service-periods/{servicePeriodId}")
    fun updateServicePeriod(
        @PathVariable slug: String,
        @PathVariable servicePeriodId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        session: HttpSession,
        @RequestBody request: UpsertRestaurantServicePeriodRequest
    ) = UpsertRestaurantServicePeriodResponse(
        message = "Restaurant service period updated.",
        restaurantServicePeriod = run {
            recentAuthenticationGuard.requireRecentAuthentication(session)
            restaurantConfigurationService.upsertServicePeriod(principal, slug, servicePeriodId, request.name, request.dayOfWeek, request.opensAt, request.closesAt, request.slotIntervalMinutes, request.reservationDurationMinutes, request.minPartySize, request.maxPartySize, request.bookingWindowDays, request.active)
        }
    )

    @GetMapping("/api/app/company/{slug}/restaurant-reservations")
    fun listReservations(@PathVariable slug: String, @AuthenticationPrincipal principal: AppAuthenticatedUser) =
        RestaurantReservationsResponse(restaurantConfigurationService.listReservations(principal, slug))

    @PutMapping("/api/app/company/{slug}/restaurant-reservations/{reservationId}/status")
    fun updateReservation(
        @PathVariable slug: String,
        @PathVariable reservationId: Long,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestBody request: UpdateRestaurantReservationOutcomeRequest
    ) = UpdateRestaurantReservationOutcomeResponse(
        message = "Restaurant reservation updated.",
        restaurantReservation = restaurantConfigurationService.updateReservation(principal, slug, reservationId, request.status, request.tableIds.ifEmpty { null })
    )

    @GetMapping("/api/app/company/{slug}/restaurant-floorbook")
    fun floorbook(
        @PathVariable slug: String,
        @AuthenticationPrincipal principal: AppAuthenticatedUser,
        @RequestParam date: String
    ) = RestaurantFloorbookResponse(restaurantConfigurationService.floorbook(principal, slug, date))
}
