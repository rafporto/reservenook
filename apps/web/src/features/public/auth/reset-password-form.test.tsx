import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { ResetPasswordForm } from "@/features/public/auth/reset-password-form";

const push = vi.fn();

vi.mock("next/navigation", async () => {
  const actual = await vi.importActual<typeof import("next/navigation")>("next/navigation");

  return {
    ...actual,
    useRouter: () => ({
      push
    })
  };
});

describe("ResetPasswordForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    push.mockReset();
  });

  it("shows missing token guidance", () => {
    render(<ResetPasswordForm locale="en" />);

    expect(
      screen.getByText("This password reset link is incomplete. Request a new password reset email.")
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Update password" })).toBeDisabled();
  });

  it("shows validation feedback for weak password", async () => {
    render(<ResetPasswordForm locale="en" token="valid-token" />);

    fireEvent.change(screen.getByLabelText("New password"), { target: { value: "short" } });
    fireEvent.click(screen.getByRole("button", { name: "Update password" }));

    expect(await screen.findByText("Password must contain at least 8 characters.")).toBeInTheDocument();
  });

  it("renders expired token error from backend", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "This password reset link has expired. Request a new password reset email.",
          code: "EXPIRED_TOKEN"
        }),
        {
          status: 400,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ResetPasswordForm locale="en" token="expired-token" />);

    fireEvent.change(screen.getByLabelText("New password"), { target: { value: "NewSecurePass123" } });
    fireEvent.click(screen.getByRole("button", { name: "Update password" }));

    expect(
      await screen.findByText("This password reset link has expired. Request a new password reset email.")
    ).toBeInTheDocument();
  });

  it("redirects to login after successful reset", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "Your password has been updated. Continue to login.",
          redirectTo: "/en/login"
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<ResetPasswordForm locale="en" token="valid-token" />);

    fireEvent.change(screen.getByLabelText("New password"), { target: { value: "NewSecurePass123" } });
    fireEvent.click(screen.getByRole("button", { name: "Update password" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(1);
      expect(push).toHaveBeenCalledWith("/en/login");
    });
  });
});
