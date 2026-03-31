import { Box, Chip, Paper, Stack, Typography } from "@mui/material";
import { BrandLockup } from "@/components/public/brand-lockup";
import type { SupportedLocale } from "@/lib/i18n/locales";

type PublicAuthFrameProps = {
  locale: SupportedLocale;
  eyebrow: string;
  title: string;
  description: string;
  highlights: string[];
  children: React.ReactNode;
};

export function PublicAuthFrame({
  locale,
  eyebrow,
  title,
  description,
  highlights,
  children
}: PublicAuthFrameProps) {
  return (
    <Stack direction={{ xs: "column", lg: "row" }} spacing={3} alignItems="stretch">
      <Paper
        elevation={0}
        sx={{
          flex: 1,
          p: { xs: 3, md: 4 },
          borderRadius: 6,
          border: "1px solid",
          borderColor: "rgba(77, 53, 41, 0.12)",
          background:
            "linear-gradient(180deg, rgba(255, 248, 238, 0.96) 0%, rgba(247, 237, 223, 0.94) 100%)",
          boxShadow: "0 24px 80px rgba(60, 38, 24, 0.10)"
        }}
      >
        <Stack spacing={3}>
          <BrandLockup locale={locale} width={230} height={46} />
          <Stack spacing={1.5}>
            <Typography
              variant="overline"
              sx={{ letterSpacing: "0.18em", color: "primary.main", fontWeight: 700 }}
            >
              {eyebrow}
            </Typography>
            <Typography variant="h2" component="h1" sx={{ maxWidth: 540 }}>
              {title}
            </Typography>
            <Typography color="text.secondary" sx={{ maxWidth: 520, fontSize: "1.05rem" }}>
              {description}
            </Typography>
          </Stack>

          <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
            {highlights.map((highlight) => (
              <Chip
                key={highlight}
                label={highlight}
                sx={{
                  px: 1,
                  borderRadius: 999,
                  backgroundColor: "rgba(180, 90, 56, 0.10)",
                  color: "primary.dark",
                  fontWeight: 600
                }}
              />
            ))}
          </Stack>

          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: { xs: "1fr", sm: "repeat(2, minmax(0, 1fr))" },
              gap: 2
            }}
          >
            {[
              {
                label: "Fast setup",
                value: "One shared onboarding flow"
              },
              {
                label: "Operational clarity",
                value: "Same account system across business types"
              }
            ].map((item) => (
              <Box
                key={item.label}
                sx={{
                  p: 2.5,
                  borderRadius: 4,
                  backgroundColor: "rgba(255,255,255,0.56)",
                  border: "1px solid rgba(77, 53, 41, 0.10)"
                }}
              >
                <Typography variant="body2" color="text.secondary">
                  {item.label}
                </Typography>
                <Typography variant="h6" sx={{ mt: 0.5 }}>
                  {item.value}
                </Typography>
              </Box>
            ))}
          </Box>
        </Stack>
      </Paper>

      <Paper
        elevation={0}
        sx={{
          width: { xs: "100%", lg: 520 },
          borderRadius: 6,
          border: "1px solid",
          borderColor: "rgba(77, 53, 41, 0.12)",
          backgroundColor: "rgba(255, 252, 247, 0.94)",
          boxShadow: "0 24px 80px rgba(60, 38, 24, 0.08)",
          p: { xs: 3, md: 4 }
        }}
      >
        {children}
      </Paper>
    </Stack>
  );
}
