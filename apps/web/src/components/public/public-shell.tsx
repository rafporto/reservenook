import Link from "next/link";
import { AppBar, Box, Button, Container, Stack, Toolbar, Typography } from "@mui/material";
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
    <Box sx={{ minHeight: "100vh" }}>
      <AppBar
        position="static"
        color="transparent"
        elevation={0}
        sx={{ borderBottom: "1px solid", borderColor: "divider", backdropFilter: "blur(10px)" }}
      >
        <Container maxWidth="lg">
          <Toolbar disableGutters sx={{ minHeight: 80, gap: 2, justifyContent: "space-between" }}>
            <Stack direction="row" spacing={3} alignItems="center">
              <Typography variant="h6" component={Link} href={`/${locale}`} sx={{ fontWeight: 700 }}>
                Reservenook
              </Typography>
              <Button component={Link} href={`/${locale}`} color="inherit">
                {navigation.product}
              </Button>
              <Button component={Link} href={`/${locale}/register`} color="inherit">
                {navigation.register}
              </Button>
            </Stack>
            <Stack direction="row" spacing={2} alignItems="center">
              <LanguageSelector locale={locale} label={navigation.localeLabel} />
              <Button variant="outlined" color="primary" disabled>
                {navigation.login}
              </Button>
            </Stack>
          </Toolbar>
        </Container>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 5, md: 8 } }}>
        {children}
      </Container>
    </Box>
  );
}
