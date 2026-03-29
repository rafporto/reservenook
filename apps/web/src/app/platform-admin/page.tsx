import { Box, Paper, Stack, Typography } from "@mui/material";

export default function PlatformAdminPage() {
  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper elevation={0} sx={{ maxWidth: 960, mx: "auto", p: 5, border: "1px solid", borderColor: "divider" }}>
        <Stack spacing={2}>
          <Typography variant="h3" component="h1">
            Platform Admin
          </Typography>
          <Typography color="text.secondary">
            UC-04 routes authenticated platform admins here. UC-09 will implement the actual company list.
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}
