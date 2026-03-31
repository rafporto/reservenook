import Link from "next/link";
import { AppBar, Box, Button, Chip, Container, Stack, Toolbar, Typography } from "@mui/material";
import { BrandLockup } from "@/components/public/brand-lockup";
import { LanguageSelector } from "@/components/public/language-selector";
import type { SupportedLocale } from "@/lib/i18n/locales";

type PublicShellProps = {
  children: React.ReactNode;
  locale: SupportedLocale;
  navigation: {
    localeLabel: string;
    product: string;
    register: string;
    login: string;
  };
};

export function PublicShell({ children, locale, navigation }: PublicShellProps) {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        position: "relative",
        overflow: "hidden"
      }}
    >
      <Box
        aria-hidden
        sx={{
          position: "absolute",
          inset: 0,
          pointerEvents: "none",
          background:
            "radial-gradient(circle at 10% 10%, rgba(212, 154, 82, 0.18), transparent 22%), radial-gradient(circle at 90% 8%, rgba(180, 90, 56, 0.10), transparent 18%)"
        }}
      />
      <AppBar
        position="sticky"
        color="transparent"
        elevation={0}
        sx={{
          borderBottom: "1px solid",
          borderColor: "rgba(83, 58, 43, 0.10)",
          backdropFilter: "blur(16px)",
          backgroundColor: "rgba(255, 250, 244, 0.74)"
        }}
      >
        <Container maxWidth="lg">
          <Toolbar
            disableGutters
            sx={{
              minHeight: 84,
              gap: 2,
              justifyContent: "space-between",
              flexWrap: { xs: "wrap", md: "nowrap" },
              py: { xs: 1.5, md: 0 }
            }}
          >
            <Stack direction="row" spacing={3} alignItems="center" flexWrap="wrap" useFlexGap>
              <BrandLockup locale={locale} width={214} height={42} />
              <Button component={Link} href={`/${locale}`} color="inherit" sx={{ color: "text.secondary" }}>
                {navigation.product}
              </Button>
              <Button component={Link} href={`/${locale}/register`} color="inherit" sx={{ color: "text.secondary" }}>
                {navigation.register}
              </Button>
              <Chip
                label="Multi-tenant booking"
                size="small"
                sx={{
                  display: { xs: "none", md: "inline-flex" },
                  backgroundColor: "rgba(180, 90, 56, 0.08)",
                  color: "primary.dark",
                  fontWeight: 700
                }}
              />
            </Stack>
            <Stack direction="row" spacing={2} alignItems="center">
              <LanguageSelector locale={locale} label={navigation.localeLabel} />
              <Button component={Link} href={`/${locale}/login`} variant="contained" color="primary">
                {navigation.login}
              </Button>
            </Stack>
          </Toolbar>
        </Container>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 4, md: 7 }, position: "relative" }}>
        {children}
      </Container>
      <Container maxWidth="lg" sx={{ pb: 6, position: "relative" }}>
        <Box
          component="footer"
          sx={{
            borderTop: "1px solid rgba(83, 58, 43, 0.10)",
            pt: 3,
            display: "flex",
            justifyContent: "space-between",
            gap: 2,
            flexDirection: { xs: "column", md: "row" }
          }}
        >
          <Typography color="text.secondary">
            ReserveNook helps companies manage appointments, classes, and restaurant reservations in one place.
          </Typography>
          <Typography color="text.secondary">Professional booking operations with multilingual onboarding.</Typography>
        </Box>
      </Container>
    </Box>
  );
}
