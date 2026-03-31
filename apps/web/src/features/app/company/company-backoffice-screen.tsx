"use client";

import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Chip,
  CircularProgress,
  Grid,
  Paper,
  Stack,
  Typography
} from "@mui/material";
import { useRouter } from "next/navigation";
import { LogoutButton } from "@/components/app/logout-button";

type CompanyBackofficeData = {
  company: {
    companyName: string;
    companySlug: string;
    businessType: string;
    companyStatus: string;
    defaultLanguage: string;
    defaultLocale: string;
    createdAt: string;
  };
  viewer: {
    role: string;
    currentUserEmail: string;
  };
  operations: {
    planType: string;
    subscriptionExpiresAt: string | null;
    staffCount: number;
    adminCount: number;
    lastActivityAt: string;
    deletionScheduledAt: string | null;
  };
  configurationAreas: Array<{
    key: string;
    title: string;
    description: string;
    status: string;
  }>;
};

type CompanyBackofficeScreenProps = {
  slug: string;
};

function formatUtcDateTime(value: string | null) {
  if (!value) {
    return "Not scheduled";
  }

  return new Date(value).toLocaleString("en-GB", { timeZone: "UTC" });
}

export function CompanyBackofficeScreen({ slug }: CompanyBackofficeScreenProps) {
  const router = useRouter();
  const [state, setState] = useState<
    | { status: "loading" }
    | { status: "loaded"; data: CompanyBackofficeData }
    | { status: "forbidden" }
    | { status: "error"; message: string }
  >({ status: "loading" });

  useEffect(() => {
    let isMounted = true;

    async function loadBackoffice() {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/app/company/${slug}/backoffice`,
          {
            credentials: "include"
          }
        );

        if (!isMounted) {
          return;
        }

        if (response.status === 401) {
          router.replace("/en/login");
          return;
        }

        if (response.status === 403) {
          setState({ status: "forbidden" });
          return;
        }

        if (!response.ok) {
          setState({ status: "error", message: "The company backoffice could not be loaded." });
          return;
        }

        const payload = (await response.json()) as CompanyBackofficeData;
        setState({ status: "loaded", data: payload });
      } catch {
        if (!isMounted) {
          return;
        }

        setState({ status: "error", message: "The company backoffice could not be loaded." });
      }
    }

    void loadBackoffice();

    return () => {
      isMounted = false;
    };
  }, [router, slug]);

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper
        elevation={0}
        sx={{
          maxWidth: 1180,
          mx: "auto",
          p: { xs: 3, md: 5 },
          border: "1px solid",
          borderColor: "divider",
          background:
            "linear-gradient(180deg, rgba(255, 250, 244, 0.95) 0%, rgba(250, 244, 235, 0.88) 100%)"
        }}
      >
        {state.status === "loading" ? (
          <Stack spacing={2} alignItems="flex-start">
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Stack direction="row" spacing={2} alignItems="center">
              <CircularProgress size={24} />
              <Typography color="text.secondary">Loading company configuration dashboard...</Typography>
            </Stack>
          </Stack>
        ) : null}

        {state.status === "forbidden" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Alert severity="error">Access denied for this company scope.</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "error" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Alert severity="error">{state.message}</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "loaded" ? (
          <Stack spacing={4}>
            <Stack spacing={1.5}>
              <Chip
                label={`${state.data.company.businessType} tenant`}
                sx={{
                  alignSelf: "flex-start",
                  backgroundColor: "rgba(180, 90, 56, 0.10)",
                  color: "primary.dark",
                  fontWeight: 700
                }}
              />
              <Typography variant="h3" component="h1">
                {state.data.company.companyName}
              </Typography>
              <Typography color="text.secondary" sx={{ maxWidth: 760 }}>
                Shared company configuration dashboard for tenant-scoped setup, profile management, and the next
                operational features in Phase 2.
              </Typography>
            </Stack>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 7 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                  <Stack spacing={2.5}>
                    <Typography variant="h5">Company Summary</Typography>
                    <Grid container spacing={2}>
                      {[
                        { label: "Slug", value: state.data.company.companySlug },
                        { label: "Status", value: state.data.company.companyStatus },
                        { label: "Default language", value: state.data.company.defaultLanguage },
                        { label: "Default locale", value: state.data.company.defaultLocale },
                        { label: "Signed in as", value: state.data.viewer.currentUserEmail },
                        { label: "Role", value: state.data.viewer.role }
                      ].map((item) => (
                        <Grid key={item.label} size={{ xs: 12, sm: 6 }}>
                          <Paper
                            elevation={0}
                            sx={{
                              p: 2,
                              height: "100%",
                              borderRadius: 4,
                              backgroundColor: "rgba(255,255,255,0.64)",
                              border: "1px solid rgba(83, 58, 43, 0.10)"
                            }}
                          >
                            <Typography variant="body2" color="text.secondary">
                              {item.label}
                            </Typography>
                            <Typography variant="h6" sx={{ mt: 0.5 }}>
                              {item.value}
                            </Typography>
                          </Paper>
                        </Grid>
                      ))}
                    </Grid>
                  </Stack>
                </Paper>
              </Grid>

              <Grid size={{ xs: 12, md: 5 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                  <Stack spacing={2.5}>
                    <Typography variant="h5">Operational Snapshot</Typography>
                    {[
                      { label: "Plan", value: state.data.operations.planType },
                      { label: "Subscription expires", value: formatUtcDateTime(state.data.operations.subscriptionExpiresAt) },
                      { label: "Staff users", value: String(state.data.operations.staffCount) },
                      { label: "Company admins", value: String(state.data.operations.adminCount) },
                      { label: "Last activity", value: formatUtcDateTime(state.data.operations.lastActivityAt) },
                      { label: "Deletion scheduled", value: formatUtcDateTime(state.data.operations.deletionScheduledAt) }
                    ].map((item) => (
                      <Box
                        key={item.label}
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                          gap: 2,
                          py: 1.25,
                          borderBottom: "1px solid rgba(83, 58, 43, 0.08)"
                        }}
                      >
                        <Typography color="text.secondary">{item.label}</Typography>
                        <Typography sx={{ fontWeight: 700, textAlign: "right" }}>{item.value}</Typography>
                      </Box>
                    ))}
                  </Stack>
                </Paper>
              </Grid>
            </Grid>

            <Stack spacing={2}>
              <Typography variant="h4" component="h2">
                Shared Configuration Areas
              </Typography>
              <Typography color="text.secondary">
                These sections define the Phase 2 backoffice baseline. Available items can be implemented next without
                changing the tenant navigation model.
              </Typography>
              <Grid container spacing={2.5}>
                {state.data.configurationAreas.map((area) => (
                  <Grid key={area.key} size={{ xs: 12, md: 6, xl: 4 }}>
                    <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                      <Stack spacing={1.5}>
                        <Chip
                          label={area.status === "available" ? "Available now" : "Planned next"}
                          size="small"
                          sx={{
                            alignSelf: "flex-start",
                            backgroundColor:
                              area.status === "available" ? "rgba(53, 95, 89, 0.12)" : "rgba(212, 154, 82, 0.18)",
                            color: area.status === "available" ? "secondary.main" : "primary.dark",
                            fontWeight: 700
                          }}
                        />
                        <Typography variant="h6">{area.title}</Typography>
                        <Typography color="text.secondary">{area.description}</Typography>
                      </Stack>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            </Stack>

            <LogoutButton />
          </Stack>
        ) : null}
      </Paper>
    </Box>
  );
}
