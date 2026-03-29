import Link from "next/link";
import { Box, Button, Card, CardContent, Grid, Paper, Stack, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type PublicHomePageProps = {
  locale: SupportedLocale;
};

export function PublicHomePage({ locale }: PublicHomePageProps) {
  const messages = getPublicMessages(locale);

  return (
    <Stack spacing={5}>
      <Paper
        elevation={0}
        sx={{
          border: "1px solid",
          borderColor: "divider",
          borderRadius: 6,
          px: { xs: 3, md: 6 },
          py: { xs: 5, md: 8 },
          background:
            "linear-gradient(145deg, rgba(255,250,242,0.96) 0%, rgba(244,236,224,0.92) 100%)"
        }}
      >
        <Stack spacing={3}>
          <Typography variant="overline" sx={{ letterSpacing: "0.14em", color: "primary.main" }}>
            {messages.heroEyebrow}
          </Typography>
          <Typography variant="h2" component="h1" sx={{ maxWidth: 920 }}>
            {messages.heroTitle}
          </Typography>
          <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 760 }}>
            {messages.heroDescription}
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <Button component={Link} href={`/${locale}/register`} variant="contained" size="large">
              {messages.heroPrimaryCta}
            </Button>
            <Button component={Link} href="#scope" variant="outlined" size="large">
              {messages.heroSecondaryCta}
            </Button>
          </Stack>
        </Stack>
      </Paper>

      <Box id="scope">
        <Typography variant="h4" component="h2" sx={{ mb: 3 }}>
          {messages.spotlightTitle}
        </Typography>
        <Grid container spacing={3}>
          {messages.spotlightItems.map((item) => (
            <Grid key={item} size={{ xs: 12, md: 4 }}>
              <Card elevation={0} sx={{ height: "100%", border: "1px solid", borderColor: "divider" }}>
                <CardContent>
                  <Typography variant="body1">{item}</Typography>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Stack>
  );
}
