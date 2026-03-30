import { Paper, Stack, Typography } from "@mui/material";
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
            {messages.resetPasswordTitle}
          </Typography>
          <Typography color="text.secondary">{messages.resetPasswordDescription}</Typography>
        </Stack>
        <ResetPasswordForm locale={locale} token={token} />
      </Stack>
    </Paper>
  );
}
