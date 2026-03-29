import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { ResendActivationForm } from "@/features/public/activation/resend-activation-form";

describe("ResendActivationForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows validation feedback for invalid email", async () => {
    render(<ResendActivationForm locale="en" />);

    fireEvent.click(screen.getByRole("button", { name: "Send activation email" }));

    expect(await screen.findByText("This field is required.")).toBeInTheDocument();
  });

  it("shows neutral confirmation after submit", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "If the account is pending activation, a new activation email will be sent."
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ResendActivationForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Registration email"), {
      target: { value: "admin@acme.com" }
    });
    fireEvent.click(screen.getByRole("button", { name: "Send activation email" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

    expect(
      await screen.findByText("If the account is pending activation, a new activation email will be sent.")
    ).toBeInTheDocument();
  });
});
