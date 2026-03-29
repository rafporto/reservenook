import { render, screen, waitFor } from "@testing-library/react";
import { ActivationStatus } from "@/features/public/activation/activation-status";

describe("ActivationStatus", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders success when the activation token is confirmed", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          status: "ACTIVATED",
          message: "The company account has been activated."
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ActivationStatus locale="en" token="valid-token" />);

    expect(screen.getByText("Validating your activation link...")).toBeInTheDocument();

    expect(
      await screen.findByText(
        "Your company account is active. The initial admin account is now verified."
      )
    ).toBeInTheDocument();
  });

  it("renders expired state for expired token", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          status: "EXPIRED",
          message: "The activation link has expired."
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ActivationStatus locale="en" token="expired-token" />);

    expect(
      await screen.findByText("This activation link has expired. Request a new activation email.")
    ).toBeInTheDocument();
  });

  it("renders invalid state when there is no token", async () => {
    render(<ActivationStatus locale="en" />);

    await waitFor(() => {
      expect(screen.getByText("This activation link is invalid.")).toBeInTheDocument();
    });
  });
});
