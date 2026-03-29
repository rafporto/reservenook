"use client";

import { ToggleButton, ToggleButtonGroup } from "@mui/material";
import { usePathname, useRouter } from "next/navigation";
import { supportedLocales, type SupportedLocale } from "@/lib/i18n/locales";

type LanguageSelectorProps = {
  locale: SupportedLocale;
  label: string;
};

export function LanguageSelector({ locale, label }: LanguageSelectorProps) {
  const router = useRouter();
  const pathname = usePathname();

  const handleChange = (_event: React.MouseEvent<HTMLElement>, nextLocale: SupportedLocale | null) => {
    if (!nextLocale || nextLocale === locale) {
      return;
    }

    const segments = pathname.split("/").filter(Boolean);

    if (segments.length === 0) {
      router.push(`/${nextLocale}`);
      return;
    }

    segments[0] = nextLocale;
    router.push(`/${segments.join("/")}`);
  };

  return (
    <ToggleButtonGroup
      exclusive
      size="small"
      value={locale}
      aria-label={label}
      onChange={handleChange}
      color="primary"
    >
      {supportedLocales.map((supportedLocale) => (
        <ToggleButton key={supportedLocale} value={supportedLocale} aria-label={supportedLocale}>
          {supportedLocale.toUpperCase()}
        </ToggleButton>
      ))}
    </ToggleButtonGroup>
  );
}
