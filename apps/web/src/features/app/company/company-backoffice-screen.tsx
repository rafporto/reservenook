"use client";

import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress, Paper, Stack, Typography } from "@mui/material";
import { useRouter } from "next/navigation";
import { LogoutButton } from "@/components/app/logout-button";

type CompanyBackofficeData = {
  companyName: string;
  companySlug: string;
  businessType: string;
  role: string;
  currentUserEmail: string;
};

type CompanyBackofficeScreenProps = {
  slug: string;
};

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
    }

    void loadBackoffice();

    return () => {
      isMounted = false;
    };
  }, [router, slug]);

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper elevation={0} sx={{ maxWidth: 960, mx: "auto", p: 5, border: "1px solid", borderColor: "divider" }}>
        {state.status === "loading" ? (
          <Stack spacing={2} alignItems="flex-start">
            <Typography variant="h3" component="h1">
              Company Backoffice
            </Typography>
            <Stack direction="row" spacing={2} alignItems="center">
              <CircularProgress size={24} />
              <Typography color="text.secondary">Loading tenant context...</Typography>
            </Stack>
          </Stack>
        ) : null}

        {state.status === "forbidden" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Backoffice
            </Typography>
            <Alert severity="error">Access denied for this company scope.</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "error" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Backoffice
            </Typography>
            <Alert severity="error">{state.message}</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "loaded" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Backoffice
            </Typography>
            <Typography color="text.secondary">
              UC-08 enforces tenant-scoped access for the authenticated company admin.
            </Typography>
            <Typography>
              Company: <strong>{state.data.companyName}</strong>
            </Typography>
            <Typography>
              Slug: <strong>{state.data.companySlug}</strong>
            </Typography>
            <Typography>
              Business type: <strong>{state.data.businessType}</strong>
            </Typography>
            <Typography>
              Role: <strong>{state.data.role}</strong>
            </Typography>
            <Typography>
              Signed in as: <strong>{state.data.currentUserEmail}</strong>
            </Typography>
            <LogoutButton />
          </Stack>
        ) : null}
      </Paper>
    </Box>
  );
}
