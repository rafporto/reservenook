import { Alert, Paper, Stack, Typography } from "@mui/material";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type ActivatePageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ token?: string }>;
};

export default async function ActivatePage({ params, searchParams }: ActivatePageProps) {
  const { locale } = await params;
  const safeLocale = requireSupportedLocale(locale);
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
          Account activation
        </Typography>
        <Alert severity="info">
          UC-02 will implement token validation and account activation on this page. The selected
          language route is already preserved for <strong>{safeLocale.toUpperCase()}</strong>.
        </Alert>
        <Typography color="text.secondary">
          Received token: {token ? `${token.slice(0, 8)}...` : "no token provided"}
        </Typography>
      </Stack>
    </Paper>
  );
}
