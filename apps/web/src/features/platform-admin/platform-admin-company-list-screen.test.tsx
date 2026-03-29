import { render, screen, waitFor } from "@testing-library/react";
import { PlatformAdminCompanyListScreen } from "@/features/platform-admin/platform-admin-company-list-screen";

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

describe("PlatformAdminCompanyListScreen", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    replace.mockReset();
  });

  it("renders the company list with activation and plan status", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () =>
      new Response(
        JSON.stringify({
          companies: [
            {
              companyName: "Studio Norte",
              companySlug: "studio-norte",
              businessType: "CLASS",
              activationStatus: "PENDING_ACTIVATION",
              planType: "PAID",
              expiresAt: "2027-03-20T00:00:00Z"
            },
            {
              companyName: "Acme Wellness",
              companySlug: "acme-wellness",
              businessType: "APPOINTMENT",
              activationStatus: "ACTIVE",
              planType: "TRIAL",
              expiresAt: "2026-04-05T00:00:00Z"
            }
          ]
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<PlatformAdminCompanyListScreen />);

    expect(await screen.findByText("Studio Norte")).toBeInTheDocument();
    expect(screen.getByText("PENDING_ACTIVATION")).toBeInTheDocument();
    expect(screen.getByText("TRIAL")).toBeInTheDocument();
  });

  it("shows access denied for non platform users", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () => new Response(null, { status: 403 }));

    render(<PlatformAdminCompanyListScreen />);

    expect(await screen.findByText("Access denied for the platform admin area.")).toBeInTheDocument();
  });

  it("redirects unauthenticated users to the public login page", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async () => new Response(null, { status: 401 }));

    render(<PlatformAdminCompanyListScreen />);

    await waitFor(() => {
      expect(replace).toHaveBeenCalledWith("/en/login");
    });
  });
});
