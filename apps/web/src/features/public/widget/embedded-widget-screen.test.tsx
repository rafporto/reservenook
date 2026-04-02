import { render, screen } from "@testing-library/react";
import { EmbeddedWidgetScreen } from "@/features/public/widget/embedded-widget-screen";

describe("EmbeddedWidgetScreen", () => {
  it("blocks rendering when the widget token is missing", () => {
    render(<EmbeddedWidgetScreen slug="acme" locale="en" token="" theme="minimal" />);

    expect(screen.getByText("This widget session is invalid.")).toBeInTheDocument();
  });
});
