package com.reservenook.booking.application

import com.reservenook.auth.application.AppAuthenticatedUser
import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingAuditActionType
import com.reservenook.booking.domain.BookingAuditEvent
import com.reservenook.booking.domain.BookingSource
import com.reservenook.booking.domain.BookingStatus
import com.reservenook.booking.domain.CustomerContact
import com.reservenook.booking.infrastructure.BookingAuditEventRepository
import com.reservenook.booking.infrastructure.BookingRepository
import com.reservenook.booking.infrastructure.CustomerContactRepository
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingAuditSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingNotificationTriggersSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerContactSummary
import com.reservenook.companybackoffice.application.CompanyAdminAccessService
import com.reservenook.companybackoffice.application.toBookingNotificationTriggersSummary
import com.reservenook.companybackoffice.application.toSummary
import com.reservenook.security.application.SecurityAuditService
import com.reservenook.security.domain.SecurityAuditEventType
import com.reservenook.security.domain.SecurityAuditOutcome
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BookingInfrastructureService(
    private val companyAdminAccessService: CompanyAdminAccessService,
    private val customerContactRepository: CustomerContactRepository,
    private val bookingRepository: BookingRepository,
    private val bookingAuditEventRepository: BookingAuditEventRepository,
    private val securityAuditService: SecurityAuditService
) {

    @Transactional(readOnly = true)
    fun listCustomerContacts(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeCustomerContactSummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        return customerContactRepository.findAllByCompanyIdOrderByCreatedAtAsc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional(readOnly = true)
    fun findBookingEntity(bookingId: Long): Booking =
        bookingRepository.findById(bookingId).orElseThrow { IllegalArgumentException("Booking could not be found.") }

    fun toSummary(booking: Booking): CompanyBackofficeBookingSummary = booking.toSummary()

    @Transactional
    fun createCustomerContact(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CompanyBackofficeCustomerContactSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val contact = mergeOrCreateContact(companyId = requireNotNull(company.id), company = company, fullName, email, phone, preferredLanguage, notes)

        securityAuditService.record(
            eventType = if (contact.createdAt == contact.updatedAt) SecurityAuditEventType.CUSTOMER_CONTACT_CREATED else SecurityAuditEventType.CUSTOMER_CONTACT_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = contact.email
        )
        return contact.toSummary()
    }

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
    ): CompanyBackofficeCustomerContactSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val contact = customerContactRepository.findByIdAndCompanyId(contactId, requireNotNull(company.id))
            ?: throw IllegalArgumentException("Customer contact could not be found.")

        applyContactValues(contact, fullName, email, phone, preferredLanguage, notes)
        customerContactRepository.save(contact)

        securityAuditService.record(
            eventType = SecurityAuditEventType.CUSTOMER_CONTACT_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = contact.email
        )
        return contact.toSummary()
    }

    @Transactional(readOnly = true)
    fun listBookings(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        status: String?
    ): List<CompanyBackofficeBookingSummary> {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val companyId = requireNotNull(membership.company.id)
        val bookings = if (status.isNullOrBlank()) {
            bookingRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId)
        } else {
            bookingRepository.findAllByCompanyIdAndStatusOrderByCreatedAtDesc(companyId, parseBookingStatus(status))
        }
        return bookings.map { it.toSummary() }
    }

    @Transactional
    fun updateBookingStatus(
        principal: AppAuthenticatedUser,
        requestedSlug: String,
        bookingId: Long,
        status: String,
        internalNote: String?
    ): CompanyBackofficeBookingSummary {
        val membership = companyAdminAccessService.requireCompanyMember(principal, requestedSlug)
        val company = membership.company
        val booking = bookingRepository.findByIdAndCompanyId(bookingId, requireNotNull(company.id))
            ?: throw IllegalArgumentException("Booking could not be found.")

        val nextStatus = parseBookingStatus(status)
        if (booking.status == nextStatus) {
            throw IllegalArgumentException("Booking is already in the requested status.")
        }
        if (booking.status == BookingStatus.CANCELLED && nextStatus != BookingStatus.CANCELLED) {
            throw IllegalArgumentException("Cancelled bookings cannot be reopened.")
        }
        if (booking.status == BookingStatus.COMPLETED && nextStatus != BookingStatus.COMPLETED) {
            throw IllegalArgumentException("Completed bookings cannot change status.")
        }

        val previousStatus = booking.status
        booking.status = nextStatus
        booking.internalNote = internalNote?.trim()?.ifBlank { null }
        booking.updatedAt = Instant.now()
        bookingRepository.save(booking)

        bookingAuditEventRepository.save(
            BookingAuditEvent(
                booking = booking,
                company = company,
                actionType = BookingAuditActionType.BOOKING_STATUS_UPDATED,
                actorUserId = principal.userId,
                actorEmail = principal.email,
                outcome = SecurityAuditOutcome.SUCCESS,
                details = "${previousStatus.name} -> ${nextStatus.name}"
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_STATUS_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            details = "${previousStatus.name} -> ${nextStatus.name}"
        )
        return booking.toSummary()
    }

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
    ): CompanyBackofficeBookingNotificationTriggersSummary {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        val company = membership.company

        validateEmail(destinationEmail, "Booking notification destination must be a valid email address.")
        company.notificationDestinationEmail = destinationEmail.trim().lowercase()
        company.notifyOnNewBooking = notifyOnNewBooking
        company.notifyOnBookingConfirmed = notifyOnBookingConfirmed
        company.notifyOnCancellation = notifyOnCancellation
        company.notifyOnBookingCompleted = notifyOnBookingCompleted
        company.notifyOnBookingNoShow = notifyOnBookingNoShow

        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_NOTIFICATION_TRIGGERS_UPDATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal.userId,
            actorEmail = principal.email,
            companySlug = company.slug,
            targetEmail = company.notificationDestinationEmail
        )
        return company.toBookingNotificationTriggersSummary()
    }

    @Transactional(readOnly = true)
    fun listBookingAudit(principal: AppAuthenticatedUser, requestedSlug: String): List<CompanyBackofficeBookingAuditSummary> {
        val membership = companyAdminAccessService.requireCompanyAdmin(principal, requestedSlug)
        return bookingAuditEventRepository.findAllByCompanyIdOrderByCreatedAtDesc(requireNotNull(membership.company.id)).map { it.toSummary() }
    }

    @Transactional
    fun createPublicBooking(
        companyId: Long,
        companySlug: String,
        actorEmail: String?,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        requestSummary: String?,
        preferredDateIso: String?,
        notes: String?
    ): CompanyBackofficeBookingSummary {
        throw UnsupportedOperationException("Handled by PublicBookingIntakeService.")
    }

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
    ): CompanyBackofficeBookingSummary {
        val contact = mergeOrCreateContact(companyId, company, fullName, email, phone, preferredLanguage, notes)
        val booking = bookingRepository.save(
            Booking(
                company = company,
                customerContact = contact,
                status = BookingStatus.PENDING,
                source = source,
                requestSummary = requestSummary?.trim()?.ifBlank { null },
                preferredDate = preferredDateIso?.trim()?.ifBlank { null }?.let { java.time.LocalDate.parse(it) },
                internalNote = if (source == BookingSource.BACKOFFICE) notes?.trim()?.ifBlank { null } else null
            )
        )
        bookingAuditEventRepository.save(
            BookingAuditEvent(
                booking = booking,
                company = company,
                actionType = BookingAuditActionType.BOOKING_CREATED,
                actorUserId = principal?.userId,
                actorEmail = principal?.email ?: email.trim().lowercase(),
                outcome = SecurityAuditOutcome.SUCCESS,
                details = source.name
            )
        )
        securityAuditService.record(
            eventType = SecurityAuditEventType.BOOKING_CREATED,
            outcome = SecurityAuditOutcome.SUCCESS,
            actorUserId = principal?.userId,
            actorEmail = principal?.email ?: email.trim().lowercase(),
            companySlug = companySlug,
            targetEmail = email.trim().lowercase(),
            details = source.name
        )
        return booking.toSummary()
    }

    private fun mergeOrCreateContact(
        companyId: Long,
        company: com.reservenook.registration.domain.Company,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ): CustomerContact {
        val normalizedEmail = email.trim().lowercase()
        validateEmail(normalizedEmail, "Customer email must be a valid email address.")
        validateContact(fullName, phone, preferredLanguage, notes)

        val existing = customerContactRepository.findFirstByCompanyIdAndNormalizedEmail(companyId, normalizedEmail)
        val contact = existing ?: CustomerContact(
            company = company,
            fullName = fullName.trim(),
            email = normalizedEmail,
            normalizedEmail = normalizedEmail
        )

        applyContactValues(contact, fullName, normalizedEmail, phone, preferredLanguage, notes)
        return customerContactRepository.save(contact)
    }

    private fun applyContactValues(
        contact: CustomerContact,
        fullName: String,
        email: String,
        phone: String?,
        preferredLanguage: String?,
        notes: String?
    ) {
        validateContact(fullName, phone, preferredLanguage, notes)
        val normalizedEmail = email.trim().lowercase()
        validateEmail(normalizedEmail, "Customer email must be a valid email address.")
        contact.fullName = fullName.trim()
        contact.email = normalizedEmail
        contact.normalizedEmail = normalizedEmail
        contact.phone = phone?.trim()?.ifBlank { null }
        contact.preferredLanguage = preferredLanguage?.trim()?.lowercase()?.ifBlank { null }
        contact.notes = notes?.trim()?.ifBlank { null }
        contact.updatedAt = Instant.now()
    }

    private fun validateContact(fullName: String, phone: String?, preferredLanguage: String?, notes: String?) {
        if (fullName.trim().isBlank()) {
            throw IllegalArgumentException("Customer full name is required.")
        }
        if (phone != null && phone.trim().isNotBlank() && !Regex("^[0-9+()\\-\\s]{7,}$").matches(phone.trim())) {
            throw IllegalArgumentException("Customer phone must be a valid phone number.")
        }
        if (preferredLanguage != null && preferredLanguage.trim().isNotBlank() && preferredLanguage.trim().lowercase() !in setOf("en", "de", "pt")) {
            throw IllegalArgumentException("Customer preferred language is not supported.")
        }
        if (notes != null && notes.length > 1000) {
            throw IllegalArgumentException("Customer notes must be 1000 characters or fewer.")
        }
    }

    private fun validateEmail(value: String, message: String) {
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value)) {
            throw IllegalArgumentException(message)
        }
    }

    private fun parseBookingStatus(value: String): BookingStatus =
        try {
            BookingStatus.valueOf(value.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Booking status is not supported.")
        }

}
