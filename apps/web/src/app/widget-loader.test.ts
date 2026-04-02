import { GET } from "@/app/widget-loader.js/route";

describe("widget loader route", () => {
  it("returns embeddable bootstrap script content", async () => {
    const response = GET();
    const text = await response.text();

    expect(response.headers.get("Content-Type")).toContain("application/javascript");
    expect(text).toContain("api/public/widget/");
    expect(text).toContain("iframe");
  });
});
