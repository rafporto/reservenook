import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { LoginForm } from "@/features/public/auth/login-form";

type PublicLoginPageProps = {
  locale: SupportedLocale;
};

export function PublicLoginPage({ locale }: PublicLoginPageProps) {
  const messages = getPublicMessages(locale);

  return (
    <PublicAuthFrame
      locale={locale}
      eyebrow={messages.navLogin}
      title={messages.loginTitle}
      description={messages.loginDescription}
      highlights={["Secure access", "Company admins", "Platform admins"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Access your ReserveNook workspace</Typography>
        <LoginForm locale={locale} />
      </Stack>
    </PublicAuthFrame>
  );
}
