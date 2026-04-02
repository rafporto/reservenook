"use client";

import { Alert, Box, Stack, Typography } from "@mui/material";
import { PublicBookingPage } from "@/features/public/booking/public-booking-page";
import type { SupportedLocale } from "@/lib/i18n/locales";

type Props = {
  slug: string;
  locale: SupportedLocale;
  token: string;
  theme: string;
};

export function EmbeddedWidgetScreen({ slug, locale, token, theme }: Props) {
  if (!token) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">This widget session is invalid.</Alert>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        minHeight: "100vh",
        p: 1.5,
        background: "transparent"
      }}
    >
      <Stack spacing={1.5}>
        <Typography variant="overline" sx={{ px: 1, color: "text.secondary" }}>
          ReserveNook Embedded Widget
        </Typography>
        <PublicBookingPage locale={locale} slug={slug} widgetToken={token} embedded themeVariant={theme} />
      </Stack>
    </Box>
  );
}
