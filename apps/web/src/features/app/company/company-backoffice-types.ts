export type CompanyBackofficeData = {
  company: {
    companyName: string;
    companySlug: string;
    businessType: string;
    companyStatus: string;
    defaultLanguage: string;
    defaultLocale: string;
    createdAt: string;
  };
  profile: {
    businessDescription: string | null;
    contactEmail: string | null;
    contactPhone: string | null;
    addressLine1: string | null;
    addressLine2: string | null;
    city: string | null;
    postalCode: string | null;
    countryCode: string | null;
  };
  branding: {
    displayName: string | null;
    logoUrl: string | null;
    accentColor: string | null;
    supportEmail: string | null;
    supportPhone: string | null;
  };
  localization: {
    defaultLanguage: string;
    defaultLocale: string;
    supportedLanguages: string[];
    supportedLocales: string[];
  };
  businessHours: Array<{
    id?: number | null;
    dayOfWeek: string;
    opensAt: string;
    closesAt: string;
    displayOrder: number;
  }>;
  closureDates: Array<{
    id?: number | null;
    label: string | null;
    startsOn: string;
    endsOn: string;
  }>;
  notificationPreferences: {
    destinationEmail: string | null;
    notifyOnNewBooking: boolean;
    notifyOnCancellation: boolean;
    notifyDailySummary: boolean;
  };
  bookingNotificationTriggers: {
    destinationEmail: string | null;
    notifyOnNewBooking: boolean;
    notifyOnBookingConfirmed: boolean;
    notifyOnCancellation: boolean;
    notifyOnBookingCompleted: boolean;
    notifyOnBookingNoShow: boolean;
  };
  customerContacts: Array<{
    id: number;
    fullName: string;
    email: string;
    phone: string | null;
    preferredLanguage: string | null;
    notes: string | null;
    createdAt: string;
    updatedAt: string;
  }>;
  bookings: Array<{
    id: number;
    customerContactId: number;
    customerName: string;
    customerEmail: string;
    status: string;
    source: string;
    requestSummary: string | null;
    preferredDate: string | null;
    internalNote: string | null;
    createdAt: string;
    updatedAt: string;
  }>;
  bookingAudit: Array<{
    id: number;
    bookingId: number;
    actionType: string;
    actorEmail: string | null;
    outcome: string;
    details: string | null;
    createdAt: string;
  }>;
  appointmentServices: Array<{
    id: number;
    name: string;
    description: string | null;
    durationMinutes: number;
    bufferMinutes: number;
    priceLabel: string | null;
    enabled: boolean;
    autoConfirm: boolean;
  }>;
  appointmentProviders: Array<{
    id: number;
    linkedUserId: number | null;
    displayName: string;
    email: string | null;
    active: boolean;
  }>;
  providerSchedules: Array<{
    providerId: number;
    providerName: string;
    availability: Array<{
      dayOfWeek: string;
      opensAt: string;
      closesAt: string;
      displayOrder: number;
    }>;
  }>;
  staffUsers: Array<{
    membershipId: number;
    userId?: number;
    fullName: string | null;
    email: string;
    role: string;
    status: string;
    emailVerified: boolean;
    createdAt?: string;
  }>;
  customerQuestions: Array<{
    id?: number | null;
    label: string;
    questionType: string;
    required: boolean;
    enabled: boolean;
    displayOrder: number;
    options: string[];
  }>;
  widgetSettings: {
    ctaLabel: string | null;
    widgetEnabled: boolean;
    allowedDomains: string[];
    themeVariant: string;
  };
  viewer: { role: string; currentUserEmail: string };
  operations: {
    planType: string;
    subscriptionExpiresAt: string | null;
    staffCount: number;
    adminCount: number;
    lastActivityAt: string;
    deletionScheduledAt: string | null;
  };
  configurationAreas: Array<{ key: string; title: string; description: string; status: string }>;
};

export type Feedback = Record<string, { type: "success" | "error"; message: string } | null>;
export type State =
  | { status: "loading" }
  | { status: "loaded"; data: CompanyBackofficeData }
  | { status: "forbidden" }
  | { status: "error"; message: string };

export type Drafts = {
  profile: {
    companyName: string;
    businessDescription: string;
    contactEmail: string;
    contactPhone: string;
    addressLine1: string;
    addressLine2: string;
    city: string;
    postalCode: string;
    countryCode: string;
  };
  branding: {
    displayName: string;
    logoUrl: string;
    accentColor: string;
    supportEmail: string;
    supportPhone: string;
  };
  localization: {
    defaultLanguage: string;
    defaultLocale: string;
  };
  businessHours: Array<{
    dayOfWeek: string;
    opensAt: string;
    closesAt: string;
    displayOrder: number;
  }>;
  closureDates: Array<{
    label: string;
    startsOn: string;
    endsOn: string;
  }>;
  notifications: {
    destinationEmail: string;
    notifyOnNewBooking: boolean;
    notifyOnCancellation: boolean;
    notifyDailySummary: boolean;
  };
  bookingTriggers: {
    destinationEmail: string;
    notifyOnNewBooking: boolean;
    notifyOnBookingConfirmed: boolean;
    notifyOnCancellation: boolean;
    notifyOnBookingCompleted: boolean;
    notifyOnBookingNoShow: boolean;
  };
  contactCreate: {
    fullName: string;
    email: string;
    phone: string;
    preferredLanguage: string;
    notes: string;
  };
  contactUpdate: Record<number, {
    fullName: string;
    email: string;
    phone: string;
    preferredLanguage: string;
    notes: string;
  }>;
  staffCreate: {
    fullName: string;
    email: string;
    role: string;
  };
  appointmentServiceCreate: {
    name: string;
    description: string;
    durationMinutes: string;
    bufferMinutes: string;
    priceLabel: string;
    enabled: boolean;
    autoConfirm: boolean;
  };
  appointmentServiceUpdate: Record<number, {
    name: string;
    description: string;
    durationMinutes: string;
    bufferMinutes: string;
    priceLabel: string;
    enabled: boolean;
    autoConfirm: boolean;
  }>;
  appointmentProviderCreate: {
    linkedUserId: string;
    displayName: string;
    email: string;
    active: boolean;
  };
  appointmentProviderUpdate: Record<number, {
    linkedUserId: string;
    displayName: string;
    email: string;
    active: boolean;
  }>;
  providerAvailabilityUpdate: Record<number, Array<{
    dayOfWeek: string;
    opensAt: string;
    closesAt: string;
    displayOrder: number;
  }>>;
  staffUpdate: Record<number, { role: string; status: string }>;
  bookingUpdate: Record<number, { status: string; internalNote: string }>;
  questions: Array<{
    label: string;
    questionType: string;
    required: boolean;
    enabled: boolean;
    displayOrder: number;
    optionsText: string;
  }>;
  widget: {
    ctaLabel: string;
    widgetEnabled: boolean;
    allowedDomainsText: string;
    themeVariant: string;
  };
};

export type ApiMessageResponse = { message?: string } | null;

export const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
export const roles = ["COMPANY_ADMIN", "STAFF"];
export const statuses = ["ACTIVE", "INACTIVE"];
export const bookingStatuses = ["PENDING", "CONFIRMED", "CANCELLED", "COMPLETED", "NO_SHOW"];
export const questionTypes = ["SHORT_TEXT", "LONG_TEXT", "SINGLE_SELECT", "CHECKBOX"];
export const widgetThemes = ["minimal", "soft", "contrast"];

export const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const phonePattern = /^[0-9+()\-\s]{7,}$/;
export const colorPattern = /^#[0-9A-Fa-f]{6}$/;
export const domainPattern = /^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,}$/;

export function formatUtcDateTime(value: string | null) {
  return value ? new Date(value).toLocaleString("en-GB", { timeZone: "UTC" }) : "Not scheduled";
}

export function textList(value: string[]) {
  return value.join("\n");
}

export function splitTextList(value: string) {
  return value.split("\n").map((item) => item.trim()).filter(Boolean);
}
