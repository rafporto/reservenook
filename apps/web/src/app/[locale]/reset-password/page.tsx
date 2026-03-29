import { Alert, Paper, Stack, Typography } from "@mui/material";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type ResetPasswordPageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ token?: string }>;
};

export default async function ResetPasswordPage({ params, searchParams }: ResetPasswordPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);
  const messages = getPublicMessages(locale);
  const { token } = await searchParams;

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
        <Typography variant="h3" component="h1">
          {messages.resetPasswordTitle}
        </Typography>
        <Alert severity="info">{messages.resetPasswordDescription}</Alert>
        <Typography color="text.secondary">
          Received token: {token ? `${token.slice(0, 8)}...` : "no token provided"}
        </Typography>
      </Stack>
    </Paper>
  );
}
