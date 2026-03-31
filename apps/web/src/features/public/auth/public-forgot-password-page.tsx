import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { ForgotPasswordForm } from "@/features/public/auth/forgot-password-form";

type PublicForgotPasswordPageProps = {
  locale: SupportedLocale;
};

export function PublicForgotPasswordPage({ locale }: PublicForgotPasswordPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <PublicAuthFrame
      locale={locale}
      eyebrow={messages.loginForgotPasswordCta}
      title={messages.forgotPasswordTitle}
      description={messages.forgotPasswordDescription}
      highlights={["Secure reset", "Localized emails", "Fast recovery"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Request a password reset email for eligible accounts</Typography>
        <ForgotPasswordForm locale={locale} />
      </Stack>
    </PublicAuthFrame>
  );
}
