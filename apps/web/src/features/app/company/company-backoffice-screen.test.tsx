import { fireEvent, render, screen, waitFor } from "@testing-library/react";
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
    staffUsers: [
      {
        membershipId: 1,
        fullName: "Admin User",
        email: "admin@acme.com",
        role: "COMPANY_ADMIN",
        status: "ACTIVE",
        emailVerified: true
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
  });

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

  it("saves a valid company profile update", async () => {
    const fetchSpy = vi
      .spyOn(global, "fetch")
      .mockResolvedValueOnce(
        new Response(JSON.stringify(backofficePayload()), { status: 200, headers: { "Content-Type": "application/json" } })
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ token: "csrf-token" }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            message: "Company profile updated.",
            company: { ...backofficePayload().company, companyName: "Acme Wellness Studio" },
            profile: { ...backofficePayload().profile, contactEmail: "studio@acme.com" }
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByDisplayValue("hello@acme.com")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Company name"), { target: { value: "Acme Wellness Studio" } });
    fireEvent.change(screen.getByLabelText("Primary contact email"), { target: { value: "studio@acme.com" } });
    fireEvent.click(screen.getByRole("button", { name: "Save company profile" }));

    expect(await screen.findByText("Company profile updated.")).toBeInTheDocument();
    expect(fetchSpy.mock.calls[2]?.[1]?.headers).toEqual({
      "Content-Type": "application/json",
      "X-CSRF-TOKEN": "csrf-token"
    });
  });

  it("creates a staff user with the secure invitation flow", async () => {
    vi.spyOn(global, "fetch")
      .mockResolvedValueOnce(
        new Response(JSON.stringify(backofficePayload()), { status: 200, headers: { "Content-Type": "application/json" } })
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ token: "csrf-token" }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            message: "Staff user created.",
            staffUser: {
              membershipId: 2,
              fullName: "Support Agent",
              email: "staff@acme.com",
              role: "STAFF",
              status: "ACTIVE",
              emailVerified: true
            }
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByText("Create Staff User")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Full name"), { target: { value: "Support Agent" } });
    fireEvent.change(screen.getByLabelText(/^Email$/), { target: { value: "staff@acme.com" } });
    fireEvent.click(screen.getByRole("button", { name: "Create staff user" }));

    expect(await screen.findByText("Staff user created.")).toBeInTheDocument();
    expect(screen.getByText("Support Agent")).toBeInTheDocument();
  });
});
