package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingSource
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingAuditSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingNotificationTriggersSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerContactSummary
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookingInfrastructureService(
    private val customerContactService: CustomerContactService,
    private val bookingCreationService: BookingCreationService,
    private val bookingStatusService: BookingStatusService,
    private val bookingNotificationTriggerService: BookingNotificationTriggerService,
    private val bookingAuditQueryService: BookingAuditQueryService
) {

    @Transactional(readOnly = true)
    fun listCustomerContacts(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeCustomerContactSummary> =
        customerContactService.list(principal, requestedSlug)

    @Transactional(readOnly = true)
    fun findBookingEntity(bookingId: Long): Booking = bookingCreationService.findBookingEntity(bookingId)

    fun toSummary(booking: Booking): CompanyBackofficeBookingSummary = bookingCreationService.toSummary(booking)

    @Transactional
    fun createCustomerContact(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CompanyBackofficeCustomerContactSummary =
        customerContactService.create(principal, requestedSlug, fullName, email, phone, preferredLanguage, notes)

    @Transactional
    fun updateCustomerContact(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        contactId: Long,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CompanyBackofficeCustomerContactSummary =
        customerContactService.update(principal, requestedSlug, contactId, fullName, email, phone, preferredLanguage, notes)

    @Transactional(readOnly = true)
    fun listBookings(principal: AppAuthenticatedUser, requestedSlug: String, status: String?): List<CompanyBackofficeBookingSummary> =
        bookingStatusService.list(principal, requestedSlug, status)

    @Transactional
    fun updateBookingStatus(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        bookingId: Long,
        status: String,
        internalNote: String?
    ): CompanyBackofficeBookingSummary =
        bookingStatusService.update(principal, requestedSlug, bookingId, status, internalNote)

    @Transactional
    fun updateBookingNotificationTriggers(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        destinationEmail: String,
        notifyOnNewBooking: Boolean,
        notifyOnBookingConfirmed: Boolean,
        notifyOnCancellation: Boolean,
        notifyOnBookingCompleted: Boolean,
        notifyOnBookingNoShow: Boolean
    ): CompanyBackofficeBookingNotificationTriggersSummary =
        bookingNotificationTriggerService.update(
            principal,
            requestedSlug,
            destinationEmail,
            notifyOnNewBooking,
            notifyOnBookingConfirmed,
            notifyOnCancellation,
            notifyOnBookingCompleted,
            notifyOnBookingNoShow
        )

    @Transactional(readOnly = true)
    fun listBookingAudit(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeBookingAuditSummary> =
        bookingAuditQueryService.list(principal, requestedSlug)

    @Transactional
    fun createBookingInternal(
        companyId: Long,
        principal: AppAuthenticatedUser?,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        requestSummary: String?,
        preferredDateIso: String?,
        notes: String?,
        source: BookingSource,
        companySlug: String,
        company: com.reservenook.registration.domain.Company
    ): CompanyBackofficeBookingSummary =
        bookingCreationService.create(
            companyId,
            principal,
            fullName,
            email,
            phone,
            preferredLanguage,
            requestSummary,
            preferredDateIso,
            notes,
            source,
            companySlug,
            company
        )
}
