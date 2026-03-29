import { notFound } from "next/navigation";

export const supportedLocales = ["en", "de", "pt"] as const;

export type SupportedLocale = (typeof supportedLocales)[number];

export function isSupportedLocale(value: string): value is SupportedLocale {
  return supportedLocales.includes(value as SupportedLocale);
}

export function requireSupportedLocale(value: string): SupportedLocale {
  if (!isSupportedLocale(value)) {
    notFound();
  }

  return value;
}

export function defaultLocaleForLanguage(locale: SupportedLocale): string {
  switch (locale) {
    case "de":
      return "de-DE";
    case "pt":
      return "pt-PT";
    case "en":
    default:
      return "en-US";
  }
}
