import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { ResendActivationForm } from "@/features/public/activation/resend-activation-form";

type PublicResendActivationPageProps = {
  locale: SupportedLocale;
  initialEmail?: string;
};

export function PublicResendActivationPage({ locale, initialEmail }: PublicResendActivationPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <PublicAuthFrame
      locale={locale}
      eyebrow={messages.activationTitle}
      title={messages.resendActivationTitle}
      description={messages.resendActivationDescription}
      highlights={["Email verification", "Pending companies", "Admin onboarding"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Send a fresh activation link to complete company setup</Typography>
        <ResendActivationForm locale={locale} initialEmail={initialEmail} />
      </Stack>
    </PublicAuthFrame>
  );
}
