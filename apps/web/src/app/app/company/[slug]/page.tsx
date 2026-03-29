import { Box, Paper, Stack, Typography } from "@mui/material";
import { LogoutButton } from "@/components/app/logout-button";

type CompanyBackofficePageProps = {
  params: Promise<{ slug: string }>;
};

export default async function CompanyBackofficePage({ params }: CompanyBackofficePageProps) {
  const { slug } = await params;

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper elevation={0} sx={{ maxWidth: 960, mx: "auto", p: 5, border: "1px solid", borderColor: "divider" }}>
        <Stack spacing={2}>
          <Typography variant="h3" component="h1">
            Company Backoffice
          </Typography>
          <Typography color="text.secondary">
            UC-04 routes authenticated company admins to their tenant space.
          </Typography>
          <Typography>Resolved company slug: <strong>{slug}</strong></Typography>
          <LogoutButton />
        </Stack>
      </Paper>
    </Box>
  );
}
