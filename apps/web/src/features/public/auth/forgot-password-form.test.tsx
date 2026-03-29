import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { ForgotPasswordForm } from "@/features/public/auth/forgot-password-form";

describe("ForgotPasswordForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows validation feedback for invalid email", async () => {
    render(<ForgotPasswordForm locale="en" />);

    fireEvent.click(screen.getByRole("button", { name: "Send password reset email" }));

    expect(await screen.findByText("This field is required.")).toBeInTheDocument();
  });

  it("shows neutral confirmation after submit", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "If the account is eligible, a password reset email will be sent."
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ForgotPasswordForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Account email"), {
      target: { value: "admin@acme.com" }
    });
    fireEvent.click(screen.getByRole("button", { name: "Send password reset email" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

    expect(
      await screen.findByText("If the account is eligible, a password reset email will be sent.")
    ).toBeInTheDocument();
  });
});
