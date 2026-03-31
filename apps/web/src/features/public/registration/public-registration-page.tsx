import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { RegistrationForm } from "@/features/public/registration/registration-form";

type PublicRegistrationPageProps = {
  locale: SupportedLocale;
};

export function PublicRegistrationPage({ locale }: PublicRegistrationPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <PublicAuthFrame
      locale={locale}
      eyebrow={messages.navRegister}
      title={messages.registrationTitle}
      description={messages.registrationDescription}
      highlights={["Appointments", "Classes", "Restaurants"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Set up your company profile and initial admin access</Typography>
        <RegistrationForm locale={locale} />
      </Stack>
    </PublicAuthFrame>
  );
}
