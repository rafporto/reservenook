import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { ResetPasswordForm } from "@/features/public/auth/reset-password-form";

type PublicResetPasswordPageProps = {
  locale: SupportedLocale;
  token?: string;
};

export function PublicResetPasswordPage({ locale, token }: PublicResetPasswordPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <PublicAuthFrame
      locale={locale}
      eyebrow={messages.resetPasswordTitle}
      title={messages.resetPasswordTitle}
      description={messages.resetPasswordDescription}
      highlights={["Protected flow", "Short-lived token", "Account recovery"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Choose a new password to restore account access</Typography>
        <ResetPasswordForm locale={locale} token={token} />
      </Stack>
    </PublicAuthFrame>
  );
}
