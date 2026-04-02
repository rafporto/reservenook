import {
  CompanyBackofficeData,
  Drafts,
  textList
} from "@/features/app/company/company-backoffice-types";

export function buildDrafts(data: CompanyBackofficeData): Drafts {
  return {
    profile: {
      companyName: data.company.companyName,
      businessDescription: data.profile.businessDescription ?? "",
      contactEmail: data.profile.contactEmail ?? "",
      contactPhone: data.profile.contactPhone ?? "",
      addressLine1: data.profile.addressLine1 ?? "",
      addressLine2: data.profile.addressLine2 ?? "",
      city: data.profile.city ?? "",
      postalCode: data.profile.postalCode ?? "",
      countryCode: data.profile.countryCode ?? ""
    },
    branding: {
      displayName: data.branding.displayName ?? "",
      logoUrl: data.branding.logoUrl ?? "",
      accentColor: data.branding.accentColor ?? "#B45A38",
      supportEmail: data.branding.supportEmail ?? data.profile.contactEmail ?? "",
      supportPhone: data.branding.supportPhone ?? data.profile.contactPhone ?? ""
    },
    localization: {
      defaultLanguage: data.localization.defaultLanguage,
      defaultLocale: data.localization.defaultLocale
    },
    businessHours: data.businessHours.length > 0 ? data.businessHours.map((entry) => ({
      dayOfWeek: entry.dayOfWeek,
      opensAt: entry.opensAt,
      closesAt: entry.closesAt,
      displayOrder: entry.displayOrder
    })) : [{ dayOfWeek: "MONDAY", opensAt: "09:00", closesAt: "17:00", displayOrder: 0 }],
    closureDates: data.closureDates.map((entry) => ({
      label: entry.label ?? "",
      startsOn: entry.startsOn,
      endsOn: entry.endsOn
    })),
    notifications: {
      destinationEmail: data.notificationPreferences.destinationEmail ?? data.profile.contactEmail ?? "",
      notifyOnNewBooking: data.notificationPreferences.notifyOnNewBooking,
      notifyOnCancellation: data.notificationPreferences.notifyOnCancellation,
      notifyDailySummary: data.notificationPreferences.notifyDailySummary
    },
    bookingTriggers: {
      destinationEmail: data.bookingNotificationTriggers.destinationEmail ?? data.profile.contactEmail ?? "",
      notifyOnNewBooking: data.bookingNotificationTriggers.notifyOnNewBooking,
      notifyOnBookingConfirmed: data.bookingNotificationTriggers.notifyOnBookingConfirmed,
      notifyOnCancellation: data.bookingNotificationTriggers.notifyOnCancellation,
      notifyOnBookingCompleted: data.bookingNotificationTriggers.notifyOnBookingCompleted,
      notifyOnBookingNoShow: data.bookingNotificationTriggers.notifyOnBookingNoShow
    },
    contactCreate: {
      fullName: "",
      email: "",
      phone: "",
      preferredLanguage: data.company.defaultLanguage,
      notes: ""
    },
    contactUpdate: Object.fromEntries(data.customerContacts.map((contact) => [contact.id, {
      fullName: contact.fullName,
      email: contact.email,
      phone: contact.phone ?? "",
      preferredLanguage: contact.preferredLanguage ?? data.company.defaultLanguage,
      notes: contact.notes ?? ""
    }])),
    staffCreate: { fullName: "", email: "", role: "STAFF" },
    appointmentServiceCreate: {
      name: "",
      description: "",
      durationMinutes: "30",
      bufferMinutes: "0",
      priceLabel: "",
      enabled: true,
      autoConfirm: false
    },
    appointmentServiceUpdate: Object.fromEntries(data.appointmentServices.map((service) => [service.id, {
      name: service.name,
      description: service.description ?? "",
      durationMinutes: String(service.durationMinutes),
      bufferMinutes: String(service.bufferMinutes),
      priceLabel: service.priceLabel ?? "",
      enabled: service.enabled,
      autoConfirm: service.autoConfirm
    }])),
    appointmentProviderCreate: {
      linkedUserId: "",
      displayName: "",
      email: "",
      active: true
    },
    appointmentProviderUpdate: Object.fromEntries(data.appointmentProviders.map((provider) => [provider.id, {
      linkedUserId: provider.linkedUserId != null ? String(provider.linkedUserId) : "",
      displayName: provider.displayName,
      email: provider.email ?? "",
      active: provider.active
    }])),
    providerAvailabilityUpdate: Object.fromEntries(data.providerSchedules.map((schedule) => [schedule.providerId, schedule.availability.map((entry) => ({
      dayOfWeek: entry.dayOfWeek,
      opensAt: entry.opensAt,
      closesAt: entry.closesAt,
      displayOrder: entry.displayOrder
    }))])),
    staffUpdate: Object.fromEntries(data.staffUsers.map((user) => [user.membershipId, { role: user.role, status: user.status }])),
    bookingUpdate: Object.fromEntries(data.bookings.map((booking) => [booking.id, { status: booking.status, internalNote: booking.internalNote ?? "" }])),
    questions: data.customerQuestions.map((question) => ({
      label: question.label,
      questionType: question.questionType,
      required: question.required,
      enabled: question.enabled,
      displayOrder: question.displayOrder,
      optionsText: textList(question.options)
    })),
    widget: {
      ctaLabel: data.widgetSettings.ctaLabel ?? "",
      widgetEnabled: data.widgetSettings.widgetEnabled,
      allowedDomainsText: textList(data.widgetSettings.allowedDomains),
      themeVariant: data.widgetSettings.themeVariant
    }
  };
}
