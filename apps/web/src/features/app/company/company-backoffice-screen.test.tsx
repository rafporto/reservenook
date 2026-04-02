import { render, screen, waitFor } from "@testing-library/react";
import { CompanyBackofficeScreen } from "@/features/app/company/company-backoffice-screen";

const replace = vi.fn();

vi.mock("next/navigation", async () => {
  const actual = await vi.importActual<typeof import("next/navigation")>("next/navigation");
  return { ...actual, useRouter: () => ({ replace }) };
});

function backofficePayload() {
  return {
    company: {
      companyName: "Acme Wellness",
      companySlug: "acme-wellness",
      businessType: "APPOINTMENT",
      companyStatus: "ACTIVE",
      defaultLanguage: "en",
      defaultLocale: "en-US",
      createdAt: "2026-03-31T10:00:00Z"
    },
    profile: {
      businessDescription: "Premium appointments and classes.",
      contactEmail: "hello@acme.com",
      contactPhone: "+49 30 555 0000",
      addressLine1: "Alexanderplatz 1",
      addressLine2: "Floor 3",
      city: "Berlin",
      postalCode: "10178",
      countryCode: "DE"
    },
    branding: {
      displayName: "Acme Wellness",
      logoUrl: "https://cdn.acme.com/logo.svg",
      accentColor: "#B45A38",
      supportEmail: "support@acme.com",
      supportPhone: "+49 30 555 0101"
    },
    localization: {
      defaultLanguage: "en",
      defaultLocale: "en-US",
      supportedLanguages: ["en", "de", "pt"],
      supportedLocales: ["en-US", "en-GB", "de-DE", "pt-PT", "pt-BR"]
    },
    businessHours: [{ dayOfWeek: "MONDAY", opensAt: "09:00", closesAt: "17:00", displayOrder: 0 }],
    closureDates: [{ label: "Holiday", startsOn: "2026-12-24", endsOn: "2026-12-26" }],
    notificationPreferences: {
      destinationEmail: "alerts@acme.com",
      notifyOnNewBooking: true,
      notifyOnCancellation: false,
      notifyDailySummary: true
    },
    bookingNotificationTriggers: {
      destinationEmail: "alerts@acme.com",
      notifyOnNewBooking: true,
      notifyOnBookingConfirmed: true,
      notifyOnCancellation: false,
      notifyOnBookingCompleted: false,
      notifyOnBookingNoShow: false
    },
    customerContacts: [
      {
        id: 11,
        fullName: "Alex Guest",
        email: "alex@example.com",
        phone: "+49 30 111 2222",
        preferredLanguage: "en",
        notes: "Prefers email updates.",
        createdAt: "2026-03-31T10:00:00Z",
        updatedAt: "2026-03-31T10:00:00Z"
      }
    ],
    bookings: [
      {
        id: 15,
        customerContactId: 11,
        customerName: "Alex Guest",
        customerEmail: "alex@example.com",
        status: "PENDING",
        source: "PUBLIC_WEB",
        requestSummary: "Initial consultation",
        preferredDate: "2026-04-05",
        internalNote: null,
        createdAt: "2026-03-31T10:00:00Z",
        updatedAt: "2026-03-31T10:00:00Z"
      }
    ],
    bookingAudit: [
      {
        id: 21,
        bookingId: 15,
        actionType: "BOOKING_CREATED",
        actorEmail: "alex@example.com",
        outcome: "SUCCESS",
        details: "PUBLIC_WEB",
        createdAt: "2026-03-31T10:00:00Z"
      }
    ],
    appointmentServices: [
      {
        id: 31,
        name: "Initial consultation",
        description: "30 minute intro session",
        durationMinutes: 30,
        bufferMinutes: 10,
        priceLabel: "EUR 60",
        enabled: true,
        autoConfirm: false
      }
    ],
    appointmentProviders: [
      {
        id: 41,
        linkedUserId: 1,
        displayName: "Anna Therapist",
        email: "anna@acme.com",
        active: true
      }
    ],
    providerSchedules: [
      {
        providerId: 41,
        providerName: "Anna Therapist",
        availability: [
          {
            dayOfWeek: "MONDAY",
            opensAt: "09:00",
            closesAt: "13:00",
            displayOrder: 0
          }
        ]
      }
    ],
    staffUsers: [
      {
        membershipId: 1,
        userId: 1,
        fullName: "Admin User",
        email: "admin@acme.com",
        role: "COMPANY_ADMIN",
        status: "ACTIVE",
        emailVerified: true,
        createdAt: "2026-03-31T10:00:00Z"
      }
    ],
    customerQuestions: [
      {
        label: "Preferred provider",
        questionType: "SINGLE_SELECT",
        required: true,
        enabled: true,
        displayOrder: 0,
        options: ["Any", "Anna"]
      }
    ],
    widgetSettings: {
      ctaLabel: "Reserve now",
      widgetEnabled: true,
      allowedDomains: ["booking.acme.com"],
      themeVariant: "soft"
    },
    viewer: { role: "COMPANY_ADMIN", currentUserEmail: "admin@acme.com" },
    operations: {
      planType: "TRIAL",
      subscriptionExpiresAt: "2026-04-07T10:00:00Z",
      staffCount: 1,
      adminCount: 1,
      lastActivityAt: "2026-03-31T10:00:00Z",
      deletionScheduledAt: null
    },
    configurationAreas: [
      {
        key: "profile",
        title: "Company profile",
        description: "Review and manage the core company identity.",
        status: "available"
      },
      {
        key: "staff",
        title: "Staff users",
        description: "Manage tenant users.",
        status: "available"
      }
    ]
  };
}

describe("CompanyBackofficeScreen", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    replace.mockReset();
  });

  it("renders tenant-safe company data for the authorized company", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(JSON.stringify(backofficePayload()), { status: 200, headers: { "Content-Type": "application/json" } })
    );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByText("Acme Wellness")).toBeInTheDocument();
    expect(screen.getByText("admin@acme.com")).toBeInTheDocument();
    expect(screen.getByText("Shared Configuration Areas")).toBeInTheDocument();
    expect(screen.getByDisplayValue("hello@acme.com")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Reserve now")).toBeInTheDocument();
    expect(screen.getByText("Appointment Services")).toBeInTheDocument();
    expect(screen.getByText("Providers And Availability")).toBeInTheDocument();
  }, 10000);

  it("shows access denied for cross-tenant access", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(new Response(null, { status: 403 }));

    render(<CompanyBackofficeScreen slug="other-company" />);

    expect(await screen.findByText("Access denied for this company scope.")).toBeInTheDocument();
  });

  it("redirects unauthenticated access to the public login page", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(new Response(null, { status: 401 }));

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/en/login"));
  });

  it("renders editable profile and staffing controls for admins", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(JSON.stringify(backofficePayload()), { status: 200, headers: { "Content-Type": "application/json" } })
    );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByText("Company Profile")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Save company profile" })).toBeInTheDocument();
    expect(screen.getByText("Create Staff User")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Create staff user" })).toBeInTheDocument();
  });

  it("renders appointment configuration controls for phase 4", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(JSON.stringify(backofficePayload()), { status: 200, headers: { "Content-Type": "application/json" } })
    );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByText("Appointment Services")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Create appointment service" })).toBeInTheDocument();
    expect(screen.getByText("Providers And Availability")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Create provider" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Save availability" })).toBeInTheDocument();
  });

});
