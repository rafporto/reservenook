import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { LoginForm } from "@/features/public/auth/login-form";

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

describe("LoginForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    push.mockReset();
  });

  it("shows validation feedback for invalid input", async () => {
    render(<LoginForm locale="en" />);

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("This field is required.")).toBeInTheDocument();
  });

  it("renders generic credential failure", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "Invalid email or password.",
          code: "INVALID_CREDENTIALS"
        }),
        {
          status: 401,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<LoginForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "admin@acme.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "SecurePass123" } });
    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Invalid email or password.")).toBeInTheDocument();
  });

  it("uses the same generic failure message for activation-blocked accounts", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "Invalid email or password.",
          code: "INVALID_CREDENTIALS"
        }),
        {
          status: 401,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<LoginForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "admin@acme.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "SecurePass123" } });
    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Invalid email or password.")).toBeInTheDocument();
  });

  it("redirects after successful login", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          redirectTo: "/app/company/acme-wellness"
        }),
        {
          status: 200,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<LoginForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "admin@acme.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "SecurePass123" } });
    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(1);
      expect(push).toHaveBeenCalledWith("/app/company/acme-wellness");
    });
  });
});
