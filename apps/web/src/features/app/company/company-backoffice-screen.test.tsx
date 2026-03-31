import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { CompanyBackofficeScreen } from "@/features/app/company/company-backoffice-screen";

const replace = vi.fn();
const router = {
  replace
};

vi.mock("next/navigation", async () => {
  const actual = await vi.importActual<typeof import("next/navigation")>("next/navigation");

  return {
    ...actual,
    useRouter: () => router
  };
});

describe("CompanyBackofficeScreen", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    replace.mockReset();
  });

  it("renders tenant-safe company data for the authorized company", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () =>
      new Response(
        JSON.stringify({
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
          viewer: {
            role: "COMPANY_ADMIN",
            currentUserEmail: "admin@acme.com"
          },
          operations: {
            planType: "TRIAL",
            subscriptionExpiresAt: "2026-04-07T10:00:00Z",
            staffCount: 3,
            adminCount: 1,
            lastActivityAt: "2026-03-31T10:00:00Z",
            deletionScheduledAt: null
          },
          configurationAreas: [
            {
              key: "profile",
              title: "Company profile",
              description: "Review and manage the core company identity, business details, and support contacts.",
              status: "available"
            }
          ]
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByText("Acme Wellness")).toBeInTheDocument();
    expect(screen.getByText("admin@acme.com")).toBeInTheDocument();
    expect(screen.getByText("Shared Configuration Areas")).toBeInTheDocument();
    expect(screen.getByText("Company profile")).toBeInTheDocument();
    expect(screen.getByDisplayValue("hello@acme.com")).toBeInTheDocument();
  });

  it("shows access denied for cross-tenant access", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () => new Response(null, { status: 403 }));

    render(<CompanyBackofficeScreen slug="other-company" />);

    expect(await screen.findByText("Access denied for this company scope.")).toBeInTheDocument();
  });

  it("redirects unauthenticated access to the public login page", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () => new Response(null, { status: 401 }));

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    await waitFor(() => {
      expect(replace).toHaveBeenCalledWith("/en/login");
    });
  });

  it("saves a valid company profile update", async () => {
    const fetchSpy = vi
      .spyOn(global, "fetch")
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
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
            viewer: {
              role: "COMPANY_ADMIN",
              currentUserEmail: "admin@acme.com"
            },
            operations: {
              planType: "TRIAL",
              subscriptionExpiresAt: "2026-04-07T10:00:00Z",
              staffCount: 3,
              adminCount: 1,
              lastActivityAt: "2026-03-31T10:00:00Z",
              deletionScheduledAt: null
            },
            configurationAreas: [
              {
                key: "profile",
                title: "Company profile",
                description: "Review and manage the core company identity, business details, and support contacts.",
                status: "available"
              }
            ]
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            message: "Company profile updated.",
            company: {
              companyName: "Acme Wellness Studio",
              companySlug: "acme-wellness",
              businessType: "APPOINTMENT",
              companyStatus: "ACTIVE",
              defaultLanguage: "en",
              defaultLocale: "en-US",
              createdAt: "2026-03-31T10:00:00Z"
            },
            profile: {
              businessDescription: "Premium appointments and classes.",
              contactEmail: "studio@acme.com",
              contactPhone: "+49 30 555 9999",
              addressLine1: "Alexanderplatz 1",
              addressLine2: "Floor 3",
              city: "Berlin",
              postalCode: "10178",
              countryCode: "DE"
            },
            viewer: {
              role: "COMPANY_ADMIN",
              currentUserEmail: "admin@acme.com"
            },
            operations: {
              planType: "TRIAL",
              subscriptionExpiresAt: "2026-04-07T10:00:00Z",
              staffCount: 3,
              adminCount: 1,
              lastActivityAt: "2026-03-31T10:00:00Z",
              deletionScheduledAt: null
            },
            configurationAreas: [
              {
                key: "profile",
                title: "Company profile",
                description: "Review and manage the core company identity, business details, and support contacts.",
                status: "available"
              }
            ]
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        )
      );

    render(<CompanyBackofficeScreen slug="acme-wellness" />);

    expect(await screen.findByDisplayValue("hello@acme.com")).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText("Company name"), { target: { value: "Acme Wellness Studio" } });
    fireEvent.change(screen.getByLabelText("Primary contact email"), { target: { value: "studio@acme.com" } });
    fireEvent.change(screen.getByLabelText("Primary contact phone"), { target: { value: "+49 30 555 9999" } });
    fireEvent.click(screen.getByRole("button", { name: "Save company profile" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(2);
    });

    expect(await screen.findByText("Company profile updated.")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Acme Wellness Studio" })).toBeInTheDocument();
  });
});
