import { Paper, Stack, Typography } from "@mui/material";
import { BrandLockup } from "@/components/public/brand-lockup";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { LoginForm } from "@/features/public/auth/login-form";

type PublicLoginPageProps = {
  locale: SupportedLocale;
};

export function PublicLoginPage({ locale }: PublicLoginPageProps) {
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
        <BrandLockup locale={locale} />
        <Stack spacing={1}>
          <Typography variant="h3" component="h1">
            {messages.loginTitle}
          </Typography>
          <Typography color="text.secondary">{messages.loginDescription}</Typography>
        </Stack>
        <LoginForm locale={locale} />
      </Stack>
    </Paper>
  );
}
