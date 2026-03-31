import type { MetadataRoute } from "next";
import { supportedLocales } from "@/lib/i18n/locales";

export default function sitemap(): MetadataRoute.Sitemap {
  const baseUrl = process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000";
  const now = new Date();

  return supportedLocales.flatMap((locale) => [
    {
      url: `${baseUrl}/${locale}`,
      lastModified: now,
      changeFrequency: "weekly",
      priority: 1
    },
    {
      url: `${baseUrl}/${locale}/register`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.8
    },
    {
      url: `${baseUrl}/${locale}/login`,
      lastModified: now,
      changeFrequency: "monthly",
      priority: 0.6
    }
  ]);
}
