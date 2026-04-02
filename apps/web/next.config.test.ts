import nextConfig, { buildSecurityHeaders } from "./next.config";

describe("next.config security headers", () => {
  it("defines browser security headers for every route", async () => {
    expect(typeof nextConfig.headers).toBe("function");

    const rules = await nextConfig.headers?.();

    expect(rules).toEqual([
      {
        source: "/widget/:path*",
        headers: buildSecurityHeaders(false, true)
      },
      {
        source: "/:path*",
        headers: buildSecurityHeaders(false)
      }
    ]);
  });

  it("adds hsts when enabled for a secure deployment", () => {
    expect(buildSecurityHeaders(true)).toContainEqual({
      key: "Strict-Transport-Security",
      value: "max-age=31536000; includeSubDomains"
    });
  });
});
