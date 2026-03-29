import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { RegistrationForm } from "@/features/public/registration/registration-form";

describe("RegistrationForm", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("shows validation feedback for invalid input", async () => {
    render(<RegistrationForm locale="en" />);

    fireEvent.click(screen.getByRole("button", { name: "Create company account" }));

    expect(await screen.findAllByText("This field is required.")).not.toHaveLength(0);
    expect(screen.getByText("Password must contain at least 8 characters.")).toBeInTheDocument();
  });

  it("submits valid registration data and shows the success message", async () => {
    const fetchSpy = vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "Registration received. Check your email to activate your account."
        }),
        {
          status: 201,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<RegistrationForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Company name"), {
      target: { value: "Acme Wellness" }
    });
    fireEvent.change(screen.getByLabelText("Public slug"), {
      target: { value: "acme-wellness" }
    });
    fireEvent.change(screen.getByLabelText("Admin email"), {
      target: { value: "admin@acme.com" }
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "SecurePass123" }
    });

    fireEvent.click(screen.getByRole("button", { name: "Create company account" }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledTimes(1);
    });

    expect(fetchSpy).toHaveBeenCalledWith(
      "http://localhost:8080/api/public/companies/registration",
      expect.objectContaining({
        method: "POST"
      })
    );
    expect(
      await screen.findByText("Registration received. Check your email to activate your account.")
    ).toBeInTheDocument();
  });

  it("renders backend conflict errors clearly", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          message: "Company slug is already in use."
        }),
        {
          status: 409,
          headers: { "Content-Type": "application/json" }
        }
      )
    );

    render(<RegistrationForm locale="en" />);

    fireEvent.change(screen.getByLabelText("Company name"), {
      target: { value: "Acme Wellness" }
    });
    fireEvent.change(screen.getByLabelText("Public slug"), {
      target: { value: "acme-wellness" }
    });
    fireEvent.change(screen.getByLabelText("Admin email"), {
      target: { value: "admin@acme.com" }
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "SecurePass123" }
    });

    fireEvent.click(screen.getByRole("button", { name: "Create company account" }));

    expect(await screen.findByText("Company slug is already in use.")).toBeInTheDocument();
  });
});
