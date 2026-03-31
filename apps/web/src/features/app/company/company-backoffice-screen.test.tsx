import { render, screen, waitFor } from "@testing-library/react";
import { CompanyBackofficeScreen } from "@/features/app/company/company-backoffice-screen";

const replace = vi.fn();

vi.mock("next/navigation", async () => {
  const actual = await vi.importActual<typeof import("next/navigation")>("next/navigation");

  return {
    ...actual,
    useRouter: () => ({
      replace
    })
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
});
