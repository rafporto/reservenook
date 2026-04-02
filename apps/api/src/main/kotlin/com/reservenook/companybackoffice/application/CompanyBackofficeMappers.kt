package com.reservenook.companybackoffice.application

import com.reservenook.companybackoffice.api.CompanyBackofficeBrandingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassInstructorSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassSessionSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClassTypeSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeDiningAreaSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantFloorbookEntrySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantReservationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantServicePeriodSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableCombinationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeRestaurantTableSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingAuditSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingNotificationTriggersSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBookingSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentProviderSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeAppointmentServiceSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeBusinessHourSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeClosureDateSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerContactSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCompanySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeCustomerQuestionSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeLocalizationSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeNotificationPreferencesSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProfileSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderAvailabilitySummary
import com.reservenook.companybackoffice.api.CompanyBackofficeProviderScheduleSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeStaffUserSummary
import com.reservenook.companybackoffice.api.CompanyBackofficeWidgetSettingsSummary
import com.reservenook.appointment.domain.AppointmentProvider
import com.reservenook.appointment.domain.AppointmentProviderAvailability
import com.reservenook.appointment.domain.AppointmentService
import com.reservenook.booking.domain.Booking
import com.reservenook.booking.domain.BookingAuditEvent
import com.reservenook.booking.domain.CustomerContact
import com.reservenook.companybackoffice.domain.CompanyBusinessHour
import com.reservenook.companybackoffice.domain.CompanyClosureDate
import com.reservenook.companybackoffice.domain.CompanyCustomerQuestion
import com.reservenook.groupclass.domain.ClassBooking
import com.reservenook.groupclass.domain.ClassInstructor
import com.reservenook.groupclass.domain.ClassSession
import com.reservenook.groupclass.domain.ClassType
import com.reservenook.restaurant.domain.DiningArea
import com.reservenook.restaurant.domain.RestaurantReservation
import com.reservenook.restaurant.domain.RestaurantServicePeriod
import com.reservenook.restaurant.domain.RestaurantTable
import com.reservenook.restaurant.domain.RestaurantTableCombination
import com.reservenook.registration.domain.Company
import com.reservenook.registration.domain.CompanyMembership

private val supportedLanguages = listOf("en", "de", "pt")
private val supportedLocales = listOf("en-US", "en-GB", "de-DE", "pt-PT", "pt-BR")

fun supportedCompanyLanguages(): List<String> = supportedLanguages

fun supportedCompanyLocales(): List<String> = supportedLocales

fun Company.toCompanySummary() = CompanyBackofficeCompanySummary(
    companyName = name,
    companySlug = slug,
    businessType = businessType.name,
    companyStatus = status.name,
    defaultLanguage = defaultLanguage,
    defaultLocale = defaultLocale,
    createdAt = createdAt.toString()
)

fun Company.toProfileSummary() = CompanyBackofficeProfileSummary(
    businessDescription = businessDescription,
    contactEmail = contactEmail,
    contactPhone = contactPhone,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    postalCode = postalCode,
    countryCode = countryCode
)

fun Company.toBrandingSummary() = CompanyBackofficeBrandingSummary(
    displayName = brandDisplayName,
    logoUrl = brandLogoUrl,
    accentColor = brandAccentColor,
    supportEmail = supportEmail,
    supportPhone = supportPhone
)

fun Company.toLocalizationSummary() = CompanyBackofficeLocalizationSummary(
    defaultLanguage = defaultLanguage,
    defaultLocale = defaultLocale,
    supportedLanguages = supportedCompanyLanguages(),
    supportedLocales = supportedCompanyLocales()
)

fun Company.toNotificationPreferencesSummary() = CompanyBackofficeNotificationPreferencesSummary(
    destinationEmail = notificationDestinationEmail,
    notifyOnNewBooking = notifyOnNewBooking,
    notifyOnCancellation = notifyOnCancellation,
    notifyDailySummary = notifyDailySummary
)

fun Company.toBookingNotificationTriggersSummary() = CompanyBackofficeBookingNotificationTriggersSummary(
    destinationEmail = notificationDestinationEmail,
    notifyOnNewBooking = notifyOnNewBooking,
    notifyOnBookingConfirmed = notifyOnBookingConfirmed,
    notifyOnCancellation = notifyOnCancellation,
    notifyOnBookingCompleted = notifyOnBookingCompleted,
    notifyOnBookingNoShow = notifyOnBookingNoShow
)

fun Company.toWidgetSettingsSummary() = CompanyBackofficeWidgetSettingsSummary(
    ctaLabel = widgetCtaLabel,
    widgetEnabled = widgetEnabled,
    allowedDomains = widgetAllowedDomains
        ?.split("\n")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList(),
    themeVariant = widgetThemeVariant
)

fun CompanyBusinessHour.toSummary() = CompanyBackofficeBusinessHourSummary(
    id = id,
    dayOfWeek = dayOfWeek.name,
    opensAt = opensAt.toString(),
    closesAt = closesAt.toString(),
    displayOrder = displayOrder
)

fun CompanyClosureDate.toSummary() = CompanyBackofficeClosureDateSummary(
    id = id,
    label = label,
    startsOn = startsOn.toString(),
    endsOn = endsOn.toString()
)

fun CompanyCustomerQuestion.toSummary() = CompanyBackofficeCustomerQuestionSummary(
    id = id,
    label = label,
    questionType = questionType.name,
    required = required,
    enabled = enabled,
    displayOrder = displayOrder,
    options = optionsText
        ?.split("\n")
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() }
        ?: emptyList()
)

fun CompanyMembership.toStaffSummary() = CompanyBackofficeStaffUserSummary(
    membershipId = requireNotNull(id),
    userId = requireNotNull(user.id),
    fullName = user.fullName,
    email = user.email,
    role = role.name,
    status = user.status.name,
    emailVerified = user.emailVerified,
    createdAt = createdAt.toString()
)

fun CustomerContact.toSummary() = CompanyBackofficeCustomerContactSummary(
    id = requireNotNull(id),
    fullName = fullName,
    email = email,
    phone = phone,
    preferredLanguage = preferredLanguage,
    notes = notes,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun Booking.toSummary() = CompanyBackofficeBookingSummary(
    id = requireNotNull(id),
    customerContactId = requireNotNull(customerContact.id),
    customerName = customerContact.fullName,
    customerEmail = customerContact.email,
    status = status.name,
    source = source.name,
    requestSummary = requestSummary,
    preferredDate = preferredDate?.toString(),
    internalNote = internalNote,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)

fun BookingAuditEvent.toSummary() = CompanyBackofficeBookingAuditSummary(
    id = requireNotNull(id),
    bookingId = requireNotNull(booking.id),
    actionType = actionType.name,
    actorEmail = actorEmail,
    outcome = outcome.name,
    details = details,
    createdAt = createdAt.toString()
)

fun AppointmentService.toSummary() = CompanyBackofficeAppointmentServiceSummary(
    id = requireNotNull(id),
    name = name,
    description = description,
    durationMinutes = durationMinutes,
    bufferMinutes = bufferMinutes,
    priceLabel = priceLabel,
    enabled = enabled,
    autoConfirm = autoConfirm
)

fun AppointmentProvider.toSummary() = CompanyBackofficeAppointmentProviderSummary(
    id = requireNotNull(id),
    linkedUserId = linkedUser?.id,
    displayName = displayName,
    email = email,
    active = active
)

fun AppointmentProviderAvailability.toSummary() = CompanyBackofficeProviderAvailabilitySummary(
    dayOfWeek = dayOfWeek.name,
    opensAt = opensAt.toString(),
    closesAt = closesAt.toString(),
    displayOrder = displayOrder
)

fun AppointmentProvider.toScheduleSummary(availability: List<AppointmentProviderAvailability>) = CompanyBackofficeProviderScheduleSummary(
    providerId = requireNotNull(id),
    providerName = displayName,
    availability = availability.sortedWith(compareBy<AppointmentProviderAvailability> { it.dayOfWeek.ordinal }.thenBy { it.displayOrder }).map { it.toSummary() }
)

fun ClassType.toSummary() = CompanyBackofficeClassTypeSummary(
    id = requireNotNull(id),
    name = name,
    description = description,
    durationMinutes = durationMinutes,
    defaultCapacity = defaultCapacity,
    active = active,
    autoConfirm = autoConfirm
)

fun ClassInstructor.toSummary() = CompanyBackofficeClassInstructorSummary(
    id = requireNotNull(id),
    linkedUserId = linkedUser?.id,
    displayName = displayName,
    email = email,
    active = active
)

fun ClassSession.toSummary(confirmedCount: Int, waitlistCount: Int) = CompanyBackofficeClassSessionSummary(
    id = requireNotNull(id),
    classTypeId = requireNotNull(classType.id),
    classTypeName = classType.name,
    instructorId = requireNotNull(instructor.id),
    instructorName = instructor.displayName,
    startsAt = startsAt.toString(),
    endsAt = endsAt.toString(),
    capacity = capacity,
    status = status.name,
    confirmedCount = confirmedCount,
    waitlistCount = waitlistCount
)

fun ClassBooking.toSummary() = CompanyBackofficeClassBookingSummary(
    id = requireNotNull(id),
    bookingId = requireNotNull(booking.id),
    classSessionId = requireNotNull(classSession.id),
    classTypeName = classSession.classType.name,
    instructorName = classSession.instructor.displayName,
    customerName = booking.customerContact.fullName,
    customerEmail = booking.customerContact.email,
    status = status.name,
    waitlistPosition = waitlistPosition,
    startsAt = classSession.startsAt.toString(),
    createdAt = createdAt.toString()
)

fun DiningArea.toSummary() = CompanyBackofficeDiningAreaSummary(
    id = requireNotNull(id),
    name = name,
    displayOrder = displayOrder,
    active = active
)

fun RestaurantTable.toSummary() = CompanyBackofficeRestaurantTableSummary(
    id = requireNotNull(id),
    diningAreaId = requireNotNull(diningArea.id),
    diningAreaName = diningArea.name,
    label = label,
    minPartySize = minPartySize,
    maxPartySize = maxPartySize,
    active = active
)

fun RestaurantTableCombination.toSummary() = CompanyBackofficeRestaurantTableCombinationSummary(
    id = requireNotNull(id),
    primaryTableId = requireNotNull(primaryTable.id),
    primaryTableLabel = primaryTable.label,
    secondaryTableId = requireNotNull(secondaryTable.id),
    secondaryTableLabel = secondaryTable.label
)

fun RestaurantServicePeriod.toSummary() = CompanyBackofficeRestaurantServicePeriodSummary(
    id = requireNotNull(id),
    name = name,
    dayOfWeek = dayOfWeek.name,
    opensAt = opensAt.toString(),
    closesAt = closesAt.toString(),
    slotIntervalMinutes = slotIntervalMinutes,
    reservationDurationMinutes = reservationDurationMinutes,
    minPartySize = minPartySize,
    maxPartySize = maxPartySize,
    bookingWindowDays = bookingWindowDays,
    active = active
)

fun RestaurantReservation.toSummary() = CompanyBackofficeRestaurantReservationSummary(
    id = requireNotNull(id),
    bookingId = requireNotNull(booking.id),
    customerName = booking.customerContact.fullName,
    customerEmail = booking.customerContact.email,
    servicePeriodName = servicePeriod.name,
    reservedAt = reservedAt.toString(),
    reservedUntil = reservedUntil.toString(),
    partySize = partySize,
    status = status.name,
    tableIds = tableAssignments.map { requireNotNull(it.restaurantTable.id) },
    tableLabels = tableAssignments.map { it.restaurantTable.label },
    areaNames = tableAssignments.map { it.restaurantTable.diningArea.name }.distinct(),
    createdAt = createdAt.toString()
)

fun RestaurantReservation.toFloorbookSummary() = CompanyBackofficeRestaurantFloorbookEntrySummary(
    reservationId = requireNotNull(id),
    customerName = booking.customerContact.fullName,
    customerEmail = booking.customerContact.email,
    reservedAt = reservedAt.toString(),
    reservedUntil = reservedUntil.toString(),
    partySize = partySize,
    status = status.name,
    tableLabels = tableAssignments.map { it.restaurantTable.label },
    areaNames = tableAssignments.map { it.restaurantTable.diningArea.name }.distinct()
)
