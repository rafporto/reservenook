import type { MetadataRoute } from "next";

export default function robots(): MetadataRoute.Robots {
  const baseUrl = process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000";

  return {
    rules: [
      {
        userAgent: "*",
        allow: ["/", "/en", "/de", "/pt", "/en/register", "/de/register", "/pt/register", "/en/login", "/de/login", "/pt/login"],
        disallow: [
          "/api/",
          "/platform-admin",
          "/app/",
          "/en/activate",
          "/de/activate",
          "/pt/activate",
          "/en/forgot-password",
          "/de/forgot-password",
          "/pt/forgot-password",
          "/en/reset-password",
          "/de/reset-password",
          "/pt/reset-password",
          "/en/resend-activation",
          "/de/resend-activation",
          "/pt/resend-activation"
        ]
      }
    ],
    sitemap: `${baseUrl}/sitemap.xml`
  };
}
