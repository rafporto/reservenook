import { fireEvent, render, screen, waitFor } from "@testing-library/react";
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
    vi.spyOn(global, "fetch").mockImplementation(async (input) => {
      if (String(input).includes("/api/platform-admin/inactivity-policy")) {
        return new Response(
          JSON.stringify({
            inactivityThresholdDays: 90,
            deletionWarningLeadDays: 14,
            updatedAt: "2026-03-30T10:00:00Z"
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        );
      }

      return new Response(
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
      );
    });

    render(<PlatformAdminCompanyListScreen />);

    expect(await screen.findByText("Studio Norte")).toBeInTheDocument();
    expect(screen.getByText("PENDING_ACTIVATION")).toBeInTheDocument();
    expect(screen.getByText("TRIAL")).toBeInTheDocument();
    expect(screen.getByDisplayValue("90")).toBeInTheDocument();
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

  it("validates the policy form before save", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockImplementation(async (input, init) => {
      if (String(input).includes("/api/platform-admin/inactivity-policy") && init?.method === "PUT") {
        return new Response(
          JSON.stringify({
            message: "Inactivity policy updated.",
            policy: {
              inactivityThresholdDays: 30,
              deletionWarningLeadDays: 12,
              updatedAt: "2026-03-30T11:00:00Z"
            }
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        );
      }

      if (String(input).includes("/api/platform-admin/inactivity-policy")) {
        return new Response(
          JSON.stringify({
            inactivityThresholdDays: 90,
            deletionWarningLeadDays: 14,
            updatedAt: "2026-03-30T10:00:00Z"
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        );
      }

      return new Response(JSON.stringify({ companies: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    });

    render(<PlatformAdminCompanyListScreen />);

    await screen.findByDisplayValue("90");
    fireEvent.change(screen.getByLabelText("Inactivity threshold (days)"), { target: { value: "30" } });
    fireEvent.change(screen.getByLabelText("Deletion warning lead time (days)"), { target: { value: "45" } });
    fireEvent.click(screen.getByRole("button", { name: "Save inactivity policy" }));

    expect(
      await screen.findByText("Deletion warning lead time cannot be greater than the inactivity threshold.")
    ).toBeInTheDocument();
    expect(
      fetchSpy.mock.calls.filter(([, init]) => init?.method === "PUT")
    ).toHaveLength(0);
  });

  it("saves a valid policy update", async () => {
    vi.spyOn(global, "fetch").mockImplementation(async (input, init) => {
      if (String(input).includes("/api/platform-admin/inactivity-policy") && init?.method === "PUT") {
        return new Response(
          JSON.stringify({
            message: "Inactivity policy updated.",
            policy: {
              inactivityThresholdDays: 120,
              deletionWarningLeadDays: 21,
              updatedAt: "2026-03-30T11:00:00Z"
            }
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        );
      }

      if (String(input).includes("/api/platform-admin/inactivity-policy")) {
        return new Response(
          JSON.stringify({
            inactivityThresholdDays: 90,
            deletionWarningLeadDays: 14,
            updatedAt: "2026-03-30T10:00:00Z"
          }),
          {
            status: 200,
            headers: { "Content-Type": "application/json" }
          }
        );
      }

      return new Response(JSON.stringify({ companies: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      });
    });

    render(<PlatformAdminCompanyListScreen />);

    await screen.findByDisplayValue("90");
    fireEvent.change(screen.getByLabelText("Inactivity threshold (days)"), { target: { value: "120" } });
    fireEvent.change(screen.getByLabelText("Deletion warning lead time (days)"), { target: { value: "21" } });
    fireEvent.click(screen.getByRole("button", { name: "Save inactivity policy" }));

    expect(await screen.findByText("Inactivity policy updated.")).toBeInTheDocument();
    const putCall = vi
      .mocked(global.fetch)
      .mock.calls.find(([, init]) => init?.method === "PUT");

    expect(putCall).toBeDefined();
    expect(putCall?.[1]?.body).toBe(JSON.stringify({ inactivityThresholdDays: 120, deletionWarningLeadDays: 21 }));
  });
});
