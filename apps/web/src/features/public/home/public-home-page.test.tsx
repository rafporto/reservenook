import { render, screen } from "@testing-library/react";
import { PublicHomePage } from "@/features/public/home/public-home-page";

describe("PublicHomePage", () => {
  it("renders the product page copy and registration CTA for the selected locale", () => {
    render(<PublicHomePage locale="de" />);

    expect(
      screen.getByRole("heading", {
        name: "Termine, Kurse und Restaurantreservierungen in einem Produkt."
      })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("link", {
        name: "Firmenregistrierung starten"
      })
    ).toHaveAttribute("href", "/de/register");
  });
});
