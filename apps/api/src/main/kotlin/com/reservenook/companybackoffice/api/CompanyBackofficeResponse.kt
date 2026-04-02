package com.reservenook.companybackoffice.api

data class CompanyBackofficeResponse(
    val company: CompanyBackofficeCompanySummary,
    val profile: CompanyBackofficeProfileSummary,
    val branding: CompanyBackofficeBrandingSummary,
    val localization: CompanyBackofficeLocalizationSummary,
    val businessHours: List<CompanyBackofficeBusinessHourSummary>,
    val closureDates: List<CompanyBackofficeClosureDateSummary>,
    val notificationPreferences: CompanyBackofficeNotificationPreferencesSummary,
    val bookingNotificationTriggers: CompanyBackofficeBookingNotificationTriggersSummary,
    val customerContacts: List<CompanyBackofficeCustomerContactSummary>,
    val bookings: List<CompanyBackofficeBookingSummary>,
    val bookingAudit: List<CompanyBackofficeBookingAuditSummary>,
    val appointmentServices: List<CompanyBackofficeAppointmentServiceSummary>,
    val appointmentProviders: List<CompanyBackofficeAppointmentProviderSummary>,
    val providerSchedules: List<CompanyBackofficeProviderScheduleSummary>,
    val classTypes: List<CompanyBackofficeClassTypeSummary>,
    val classInstructors: List<CompanyBackofficeClassInstructorSummary>,
    val classSessions: List<CompanyBackofficeClassSessionSummary>,
    val classBookings: List<CompanyBackofficeClassBookingSummary>,
    val staffUsers: List<CompanyBackofficeStaffUserSummary>,
    val customerQuestions: List<CompanyBackofficeCustomerQuestionSummary>,
    val widgetSettings: CompanyBackofficeWidgetSettingsSummary,
    val viewer: CompanyBackofficeViewerSummary,
    val operations: CompanyBackofficeOperationsSummary,
    val configurationAreas: List<CompanyBackofficeAreaSummary>
)

data class CompanyBackofficeCompanySummary(
    val companyName: String,
    val companySlug: String,
    val businessType: String,
    val companyStatus: String,
    val defaultLanguage: String,
    val defaultLocale: String,
    val createdAt: String
)

data class CompanyBackofficeProfileSummary(
    val businessDescription: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val addressLine1: String?,
    val addressLine2: String?,
    val city: String?,
    val postalCode: String?,
    val countryCode: String?
)

data class CompanyBackofficeBrandingSummary(
    val displayName: String?,
    val logoUrl: String?,
    val accentColor: String?,
    val supportEmail: String?,
    val supportPhone: String?
)

data class CompanyBackofficeLocalizationSummary(
    val defaultLanguage: String,
    val defaultLocale: String,
    val supportedLanguages: List<String>,
    val supportedLocales: List<String>
)

data class CompanyBackofficeBusinessHourSummary(
    val id: Long?,
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class CompanyBackofficeClosureDateSummary(
    val id: Long?,
    val label: String?,
    val startsOn: String,
    val endsOn: String
)

data class CompanyBackofficeNotificationPreferencesSummary(
    val destinationEmail: String?,
    val notifyOnNewBooking: Boolean,
    val notifyOnCancellation: Boolean,
    val notifyDailySummary: Boolean
)

data class CompanyBackofficeBookingNotificationTriggersSummary(
    val destinationEmail: String?,
    val notifyOnNewBooking: Boolean,
    val notifyOnBookingConfirmed: Boolean,
    val notifyOnCancellation: Boolean,
    val notifyOnBookingCompleted: Boolean,
    val notifyOnBookingNoShow: Boolean
)

data class CompanyBackofficeCustomerContactSummary(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CompanyBackofficeBookingSummary(
    val id: Long,
    val customerContactId: Long,
    val customerName: String,
    val customerEmail: String,
    val status: String,
    val source: String,
    val requestSummary: String?,
    val preferredDate: String?,
    val internalNote: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CompanyBackofficeBookingAuditSummary(
    val id: Long,
    val bookingId: Long,
    val actionType: String,
    val actorEmail: String?,
    val outcome: String,
    val details: String?,
    val createdAt: String
)

data class CompanyBackofficeAppointmentServiceSummary(
    val id: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val bufferMinutes: Int,
    val priceLabel: String?,
    val enabled: Boolean,
    val autoConfirm: Boolean
)

data class CompanyBackofficeAppointmentProviderSummary(
    val id: Long,
    val linkedUserId: Long?,
    val displayName: String,
    val email: String?,
    val active: Boolean
)

data class CompanyBackofficeProviderAvailabilitySummary(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class CompanyBackofficeProviderScheduleSummary(
    val providerId: Long,
    val providerName: String,
    val availability: List<CompanyBackofficeProviderAvailabilitySummary>
)

data class CompanyBackofficeClassTypeSummary(
    val id: Long,
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val defaultCapacity: Int,
    val active: Boolean,
    val autoConfirm: Boolean
)

data class CompanyBackofficeClassInstructorSummary(
    val id: Long,
    val linkedUserId: Long?,
    val displayName: String,
    val email: String?,
    val active: Boolean
)

data class CompanyBackofficeClassSessionSummary(
    val id: Long,
    val classTypeId: Long,
    val classTypeName: String,
    val instructorId: Long,
    val instructorName: String,
    val startsAt: String,
    val endsAt: String,
    val capacity: Int,
    val status: String,
    val confirmedCount: Int,
    val waitlistCount: Int
)

data class CompanyBackofficeClassBookingSummary(
    val id: Long,
    val bookingId: Long,
    val classSessionId: Long,
    val classTypeName: String,
    val instructorName: String,
    val customerName: String,
    val customerEmail: String,
    val status: String,
    val waitlistPosition: Int?,
    val startsAt: String,
    val createdAt: String
)

data class CompanyBackofficeStaffUserSummary(
    val membershipId: Long,
    val userId: Long,
    val fullName: String?,
    val email: String,
    val role: String,
    val status: String,
    val emailVerified: Boolean,
    val createdAt: String
)

data class CompanyBackofficeCustomerQuestionSummary(
    val id: Long?,
    val label: String,
    val questionType: String,
    val required: Boolean,
    val enabled: Boolean,
    val displayOrder: Int,
    val options: List<String>
)

data class CompanyBackofficeWidgetSettingsSummary(
    val ctaLabel: String?,
    val widgetEnabled: Boolean,
    val allowedDomains: List<String>,
    val themeVariant: String
)

data class CompanyBackofficeViewerSummary(
    val role: String,
    val currentUserEmail: String
)

data class CompanyBackofficeOperationsSummary(
    val planType: String,
    val subscriptionExpiresAt: String?,
    val staffCount: Int,
    val adminCount: Int,
    val lastActivityAt: String,
    val deletionScheduledAt: String?
)

data class CompanyBackofficeAreaSummary(
    val key: String,
    val title: String,
    val description: String,
    val status: String
)

data class UpdateCompanyProfileRequest(
    val companyName: String,
    val businessDescription: String?,
    val contactEmail: String,
    val contactPhone: String,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val postalCode: String,
    val countryCode: String
)

data class UpdateCompanyBrandingRequest(
    val displayName: String?,
    val logoUrl: String?,
    val accentColor: String,
    val supportEmail: String,
    val supportPhone: String
)

data class UpdateCompanyLocalizationRequest(
    val defaultLanguage: String,
    val defaultLocale: String
)

data class UpdateCompanyBusinessHoursRequest(
    val entries: List<UpdateCompanyBusinessHourEntryRequest>
)

data class UpdateCompanyBusinessHourEntryRequest(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class UpdateCompanyClosureDatesRequest(
    val entries: List<UpdateCompanyClosureDateEntryRequest>
)

data class UpdateCompanyClosureDateEntryRequest(
    val label: String?,
    val startsOn: String,
    val endsOn: String
)

data class UpdateCompanyNotificationPreferencesRequest(
    val destinationEmail: String,
    val notifyOnNewBooking: Boolean,
    val notifyOnCancellation: Boolean,
    val notifyDailySummary: Boolean
)

data class UpdateBookingNotificationTriggersRequest(
    val destinationEmail: String,
    val notifyOnNewBooking: Boolean,
    val notifyOnBookingConfirmed: Boolean,
    val notifyOnCancellation: Boolean,
    val notifyOnBookingCompleted: Boolean,
    val notifyOnBookingNoShow: Boolean
)

data class CreateCustomerContactRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val notes: String?
)

data class UpdateCustomerContactRequest(
    val fullName: String,
    val email: String,
    val phone: String?,
    val preferredLanguage: String?,
    val notes: String?
)

data class UpdateBookingStatusRequest(
    val status: String,
    val internalNote: String?
)

data class UpsertAppointmentServiceRequest(
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val bufferMinutes: Int,
    val priceLabel: String?,
    val enabled: Boolean,
    val autoConfirm: Boolean
)

data class UpsertAppointmentProviderRequest(
    val linkedUserId: Long?,
    val displayName: String,
    val email: String?,
    val active: Boolean
)

data class UpdateAppointmentProviderAvailabilityRequest(
    val entries: List<UpdateAppointmentProviderAvailabilityEntryRequest>
)

data class UpdateAppointmentProviderAvailabilityEntryRequest(
    val dayOfWeek: String,
    val opensAt: String,
    val closesAt: String,
    val displayOrder: Int
)

data class CreateStaffUserRequest(
    val fullName: String,
    val email: String,
    val role: String
)

data class UpsertClassTypeRequest(
    val name: String,
    val description: String?,
    val durationMinutes: Int,
    val defaultCapacity: Int,
    val active: Boolean,
    val autoConfirm: Boolean
)

data class UpsertClassInstructorRequest(
    val linkedUserId: Long?,
    val displayName: String,
    val email: String?,
    val active: Boolean
)

data class UpsertClassSessionRequest(
    val classTypeId: Long,
    val instructorId: Long,
    val startsAt: String,
    val endsAt: String,
    val capacity: Int,
    val status: String
)

data class UpdateClassBookingOutcomeRequest(
    val status: String
)

data class UpdateStaffUserRequest(
    val role: String,
    val status: String
)

data class UpdateCompanyCustomerQuestionsRequest(
    val entries: List<UpdateCompanyCustomerQuestionEntryRequest>
)

data class UpdateCompanyCustomerQuestionEntryRequest(
    val label: String,
    val questionType: String,
    val required: Boolean,
    val enabled: Boolean,
    val displayOrder: Int,
    val options: List<String> = emptyList()
)

data class UpdateCompanyWidgetSettingsRequest(
    val ctaLabel: String?,
    val widgetEnabled: Boolean,
    val allowedDomains: List<String>,
    val themeVariant: String
)

data class UpdateCompanyProfileResponse(
    val message: String,
    val company: CompanyBackofficeCompanySummary,
    val profile: CompanyBackofficeProfileSummary
)

data class UpdateCompanyBrandingResponse(
    val message: String,
    val branding: CompanyBackofficeBrandingSummary
)

data class UpdateCompanyLocalizationResponse(
    val message: String,
    val localization: CompanyBackofficeLocalizationSummary,
    val company: CompanyBackofficeCompanySummary
)

data class UpdateCompanyBusinessHoursResponse(
    val message: String,
    val businessHours: List<CompanyBackofficeBusinessHourSummary>
)

data class UpdateCompanyClosureDatesResponse(
    val message: String,
    val closureDates: List<CompanyBackofficeClosureDateSummary>
)

data class UpdateCompanyNotificationPreferencesResponse(
    val message: String,
    val notificationPreferences: CompanyBackofficeNotificationPreferencesSummary
)

data class UpdateBookingNotificationTriggersResponse(
    val message: String,
    val bookingNotificationTriggers: CompanyBackofficeBookingNotificationTriggersSummary
)

data class CustomerContactsResponse(
    val customerContacts: List<CompanyBackofficeCustomerContactSummary>
)

data class CreateCustomerContactResponse(
    val message: String,
    val customerContact: CompanyBackofficeCustomerContactSummary
)

data class UpdateCustomerContactResponse(
    val message: String,
    val customerContact: CompanyBackofficeCustomerContactSummary
)

data class BookingsResponse(
    val bookings: List<CompanyBackofficeBookingSummary>
)

data class UpdateBookingStatusResponse(
    val message: String,
    val booking: CompanyBackofficeBookingSummary
)

data class BookingAuditResponse(
    val bookingAudit: List<CompanyBackofficeBookingAuditSummary>
)

data class AppointmentServicesResponse(
    val appointmentServices: List<CompanyBackofficeAppointmentServiceSummary>
)

data class UpsertAppointmentServiceResponse(
    val message: String,
    val appointmentService: CompanyBackofficeAppointmentServiceSummary
)

data class AppointmentProvidersResponse(
    val appointmentProviders: List<CompanyBackofficeAppointmentProviderSummary>,
    val providerSchedules: List<CompanyBackofficeProviderScheduleSummary>
)

data class UpsertAppointmentProviderResponse(
    val message: String,
    val appointmentProvider: CompanyBackofficeAppointmentProviderSummary
)

data class UpdateAppointmentProviderAvailabilityResponse(
    val message: String,
    val providerSchedule: CompanyBackofficeProviderScheduleSummary
)

data class ClassTypesResponse(
    val classTypes: List<CompanyBackofficeClassTypeSummary>
)

data class UpsertClassTypeResponse(
    val message: String,
    val classType: CompanyBackofficeClassTypeSummary
)

data class ClassInstructorsResponse(
    val classInstructors: List<CompanyBackofficeClassInstructorSummary>
)

data class UpsertClassInstructorResponse(
    val message: String,
    val classInstructor: CompanyBackofficeClassInstructorSummary
)

data class ClassSessionsResponse(
    val classSessions: List<CompanyBackofficeClassSessionSummary>
)

data class UpsertClassSessionResponse(
    val message: String,
    val classSession: CompanyBackofficeClassSessionSummary
)

data class ClassBookingsResponse(
    val classBookings: List<CompanyBackofficeClassBookingSummary>
)

data class UpdateClassBookingOutcomeResponse(
    val message: String,
    val classBooking: CompanyBackofficeClassBookingSummary
)

data class StaffUsersResponse(
    val staffUsers: List<CompanyBackofficeStaffUserSummary>
)

data class CreateStaffUserResponse(
    val message: String,
    val staffUser: CompanyBackofficeStaffUserSummary
)

data class UpdateStaffUserResponse(
    val message: String,
    val staffUser: CompanyBackofficeStaffUserSummary
)

data class UpdateCompanyCustomerQuestionsResponse(
    val message: String,
    val customerQuestions: List<CompanyBackofficeCustomerQuestionSummary>
)

data class UpdateCompanyWidgetSettingsResponse(
    val message: String,
    val widgetSettings: CompanyBackofficeWidgetSettingsSummary
)
