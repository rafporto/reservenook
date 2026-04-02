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
        appointmentServices: [{ id: 7, name: "Initial consultation", description: null, durationMinutes: 30, priceLabel: "EUR 60" }],
        classTypes: []
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

  it("renders class booking availability and submits a valid class booking", async () => {
    const fetchSpy = vi.spyOn(global, "fetch")
      .mockResolvedValueOnce(new Response(JSON.stringify({
        companyName: "Acme Classes",
        companySlug: "acme-classes",
        businessType: "CLASS",
        displayName: "Acme Classes",
        defaultLanguage: "en",
        defaultLocale: "en-US",
        ctaLabel: "Reserve now",
        bookingEnabled: true,
        customerQuestions: [],
        appointmentServices: [],
        classTypes: [{ id: 3, name: "Morning Yoga", description: null, durationMinutes: 60, defaultCapacity: 12 }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        sessions: [{ sessionId: 9, classTypeId: 3, classTypeName: "Morning Yoga", instructorId: 4, instructorName: "Lena Coach", startsAt: "2026-04-10T09:00:00Z", endsAt: "2026-04-10T10:00:00Z", remainingCapacity: 5, waitlistOpen: true }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ message: "Your class booking request has been received." }), { status: 200, headers: { "Content-Type": "application/json" } }));

    render(<PublicBookingPage locale="en" slug="acme-classes" />);

    expect(await screen.findByText("Request a booking")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Full name"), { target: { value: "Alex Guest" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Class type"), { target: { value: "3" } });
    expect(await screen.findByRole("button", { name: /Lena Coach/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Lena Coach/ }));
    fireEvent.click(screen.getByRole("button", { name: "Book class" }));

    expect(await screen.findByText("Your class booking request has been received.")).toBeInTheDocument();
    await waitFor(() => expect(fetchSpy).toHaveBeenCalledTimes(3));
  });

  it("renders restaurant availability and submits a valid reservation", async () => {
    const fetchSpy = vi.spyOn(global, "fetch")
      .mockResolvedValueOnce(new Response(JSON.stringify({
        companyName: "Acme Bistro",
        companySlug: "acme-bistro",
        businessType: "RESTAURANT",
        displayName: "Acme Bistro",
        defaultLanguage: "en",
        defaultLocale: "en-US",
        ctaLabel: "Reserve now",
        bookingEnabled: true,
        customerQuestions: [],
        appointmentServices: [],
        classTypes: []
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({
        slots: [{ startsAt: "2026-04-10T18:00:00Z", servicePeriodId: 7, servicePeriodName: "Dinner", partySize: 2 }]
      }), { status: 200, headers: { "Content-Type": "application/json" } }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ message: "Your restaurant reservation has been received." }), { status: 200, headers: { "Content-Type": "application/json" } }));

    render(<PublicBookingPage locale="en" slug="acme-bistro" />);

    expect(await screen.findByText("Request a booking")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText("Full name"), { target: { value: "Alex Guest" } });
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "alex@example.com" } });
    fireEvent.change(screen.getByLabelText("Party size"), { target: { value: "2" } });
    fireEvent.change(screen.getByLabelText("Reservation date"), { target: { value: "2026-04-10" } });
    expect(await screen.findByRole("button", { name: /Dinner/ })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: /Dinner/ }));
    fireEvent.click(screen.getByRole("button", { name: "Reserve table" }));

    expect(await screen.findByText("Your restaurant reservation has been received.")).toBeInTheDocument();
    await waitFor(() => expect(fetchSpy).toHaveBeenCalledTimes(3));
  });
});
