import { Paper, Stack, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { RegistrationForm } from "@/features/public/registration/registration-form";

type PublicRegistrationPageProps = {
  locale: SupportedLocale;
};

export function PublicRegistrationPage({ locale }: PublicRegistrationPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <Paper
      elevation={0}
      sx={{
        maxWidth: 760,
        mx: "auto",
        border: "1px solid",
        borderColor: "divider",
        borderRadius: 6,
        p: { xs: 3, md: 5 },
        backgroundColor: "background.paper"
      }}
    >
      <Stack spacing={3}>
        <Stack spacing={1}>
          <Typography variant="h3" component="h1">
            {messages.registrationTitle}
          </Typography>
          <Typography color="text.secondary">{messages.registrationDescription}</Typography>
        </Stack>
        <RegistrationForm locale={locale} />
      </Stack>
    </Paper>
  );
}
