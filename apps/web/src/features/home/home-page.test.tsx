import { render, screen } from "@testing-library/react";
import { HomePage } from "./home-page";

describe("HomePage", () => {
  it("renders the scaffold headline", () => {
    render(<HomePage />);

    expect(screen.getByRole("heading", { name: /reservenook frontend scaffold/i })).toBeInTheDocument();
  });
});
