import { Box, Chip, Container, Paper, Stack, Typography } from "@mui/material";

const pillars = [
  "Appointments",
  "Classes",
  "Restaurants",
  "Multi-tenant SaaS",
  "Kotlin + Spring",
  "React + Next.js"
];

export function HomePage() {
  return (
    <Box sx={{ py: { xs: 6, md: 10 } }}>
      <Container maxWidth="lg">
        <Paper
          elevation={0}
          sx={{
            border: "1px solid",
            borderColor: "divider",
            borderRadius: 6,
            px: { xs: 3, md: 6 },
            py: { xs: 5, md: 8 },
            background:
              "linear-gradient(145deg, rgba(255,250,242,0.96) 0%, rgba(244,236,224,0.9) 100%)"
          }}
        >
          <Stack spacing={4}>
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              {pillars.map((pillar) => (
                <Chip key={pillar} label={pillar} color="primary" variant="outlined" />
              ))}
            </Stack>
            <Stack spacing={2}>
              <Typography variant="h2" component="h1">
                Reservenook frontend scaffold
              </Typography>
              <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 840 }}>
                This is the initial Next.js and MUI application shell for the unified booking
                platform. It provides the base structure for the public website, tenant booking
                pages, company backoffice, and platform administration.
              </Typography>
            </Stack>
            <Stack spacing={1}>
              <Typography variant="body1">
                Next implementation steps should focus on route groups, localization wiring,
                authentication boundaries, and the first company registration slice.
              </Typography>
              <Typography variant="body2" color="text.secondary">
                This page is intentionally simple. It exists to confirm that the application
                scaffold, theme provider, and frontend module layout are in place.
              </Typography>
            </Stack>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
