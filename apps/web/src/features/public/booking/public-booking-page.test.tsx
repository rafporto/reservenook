import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { PublicBookingPage } from "@/features/public/booking/public-booking-page";

describe("PublicBookingPage", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders booking config and submits a valid booking request", async () => {
    const fetchSpy = vi.spyOn(global, "fetch")
      .mockResolvedValueOnce(new Response(JSON.stringify({
        companyName: "Acme Wellness",
        companySlug: "acme-wellness",
        businessType: "APPOINTMENT",
        displayName: "Acme Wellness",
        defaultLanguage: "en",
        defaultLocale: "en-US",
        ctaLabel: "Reserve now",
        bookingEnabled: true,
        customerQuestions: [{ label: "Preferred provider", questionType: "SINGLE_SELECT", required: true, options: ["Any"] }],
        appointmentServices: [{ id: 7, name: "Initial consultation", description: null, durationMinutes: 30, priceLabel: "EUR 60" }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        slots: [{ serviceId: 7, providerId: 5, providerName: "Anna", startsAt: "2026-04-10T09:00:00Z", endsAt: "2026-04-10T09:30:00Z" }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ message: "Your booking request has been received." }), { status: 200, headers: { "Content-Type": "application/json" } }));

    render(<PublicBookingPage locale="en" slug="acme-wellness" />);

    expect(await screen.findByText("Book an appointment")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Full name"), { target: { value: "Alex Guest" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Service"), { target: { value: "7" } });
    fireEvent.change(screen.getByLabelText("Appointment date"), { target: { value: "2026-04-10" } });
    expect(await screen.findByRole("button", { name: /Anna/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Anna/ }));
    fireEvent.click(screen.getByRole("button", { name: "Book appointment" }));

    expect(await screen.findByText("Your booking request has been received.")).toBeInTheDocument();
    await waitFor(() => expect(fetchSpy).toHaveBeenCalledTimes(3));
  });

  it("shows unavailable state when the booking page cannot be loaded", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(new Response(null, { status: 404 }));

    render(<PublicBookingPage locale="en" slug="missing-company" />);

    expect(await screen.findByText("This booking page is currently unavailable.")).toBeInTheDocument();
  });
});
