import type { NextConfig } from "next";

const contentSecurityPolicy =
  "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'";
const widgetContentSecurityPolicy =
  "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; font-src 'self' data:; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors https: http:";

export function buildSecurityHeaders(enableHsts: boolean, embeddable = false) {
  const headers = [
    { key: "Content-Security-Policy", value: embeddable ? widgetContentSecurityPolicy : contentSecurityPolicy },
    { key: "X-Content-Type-Options", value: "nosniff" },
    { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
    { key: "Permissions-Policy", value: "camera=(), microphone=(), geolocation=()" }
  ];

  if (!embeddable) {
    headers.splice(1, 0, { key: "X-Frame-Options", value: "DENY" });
  }

  if (enableHsts) {
    headers.push({
      key: "Strict-Transport-Security",
      value: "max-age=31536000; includeSubDomains"
    });
  }

  return headers;
}

const nextConfig: NextConfig = {
  reactStrictMode: true,
  output: "standalone",
  async headers() {
    return [
      {
        source: "/widget/:path*",
        headers: buildSecurityHeaders(process.env.ENABLE_HSTS === "true", true)
      },
      {
        source: "/:path*",
        headers: buildSecurityHeaders(process.env.ENABLE_HSTS === "true")
      }
    ];
  }
};

export default nextConfig;
