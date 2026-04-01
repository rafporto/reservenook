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
        displayName: "Acme Wellness",
        defaultLanguage: "en",
        defaultLocale: "en-US",
        ctaLabel: "Reserve now",
        bookingEnabled: true,
        customerQuestions: [{ label: "Preferred provider", questionType: "SINGLE_SELECT", required: true, options: ["Any"] }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ message: "Your booking request has been received." }), { status: 200, headers: { "Content-Type": "application/json" } }));

    render(<PublicBookingPage locale="en" slug="acme-wellness" />);

    expect(await screen.findByText("Request a booking")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Full name"), { target: { value: "Alex Guest" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.click(screen.getByRole("button", { name: "Reserve now" }));

    expect(await screen.findByText("Your booking request has been received.")).toBeInTheDocument();
    await waitFor(() => expect(fetchSpy).toHaveBeenCalledTimes(2));
  });

  it("shows unavailable state when the booking page cannot be loaded", async () => {
    vi.spyOn(global, "fetch").mockResolvedValue(new Response(null, { status: 404 }));

    render(<PublicBookingPage locale="en" slug="missing-company" />);

    expect(await screen.findByText("This booking page is currently unavailable.")).toBeInTheDocument();
  });
});
