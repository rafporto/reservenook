import { Paper, Stack, Typography } from "@mui/material";
import { ActivationStatus } from "@/features/public/activation/activation-status";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type ActivatePageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ token?: string }>;
};

export default async function ActivatePage({ params, searchParams }: ActivatePageProps) {
  const { locale } = await params;
  const safeLocale = requireSupportedLocale(locale);
  const { token } = await searchParams;
  const messages = getPublicMessages(safeLocale);

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
        <Typography variant="h3" component="h1">{messages.activationTitle}</Typography>
        <ActivationStatus locale={safeLocale} token={token} />
      </Stack>
    </Paper>
  );
}
