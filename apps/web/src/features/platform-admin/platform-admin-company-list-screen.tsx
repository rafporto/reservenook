"use client";

import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from "@mui/material";
import { useRouter } from "next/navigation";
import { LogoutButton } from "@/components/app/logout-button";

type PlatformAdminCompany = {
  companyName: string;
  companySlug: string;
  businessType: string;
  activationStatus: string;
  planType: string;
  expiresAt: string;
};

type PlatformAdminCompanyListPayload = {
  companies: PlatformAdminCompany[];
};

export function PlatformAdminCompanyListScreen() {
  const router = useRouter();
  const [state, setState] = useState<
    | { status: "loading" }
    | { status: "loaded"; companies: PlatformAdminCompany[] }
    | { status: "forbidden" }
    | { status: "error"; message: string }
  >({ status: "loading" });

  useEffect(() => {
    let isMounted = true;

    async function loadCompanies() {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/platform-admin/companies`,
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
        setState({ status: "error", message: "The platform admin company list could not be loaded." });
        return;
      }

      const payload = (await response.json()) as PlatformAdminCompanyListPayload;
      setState({ status: "loaded", companies: payload.companies });
    }

    void loadCompanies();

    return () => {
      isMounted = false;
    };
  }, [router]);

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper elevation={0} sx={{ maxWidth: 1120, mx: "auto", p: 5, border: "1px solid", borderColor: "divider" }}>
        <Stack spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h3" component="h1">
              Platform Admin
            </Typography>
            <Typography color="text.secondary">
              UC-09 provides read-only visibility over registered companies without entering tenant operational flows.
            </Typography>
          </Stack>

          {state.status === "loading" ? (
            <Stack direction="row" spacing={2} alignItems="center">
              <CircularProgress size={24} />
              <Typography color="text.secondary">Loading registered companies...</Typography>
            </Stack>
          ) : null}

          {state.status === "forbidden" ? (
            <Alert severity="error">Access denied for the platform admin area.</Alert>
          ) : null}

          {state.status === "error" ? (
            <Alert severity="error">{state.message}</Alert>
          ) : null}

          {state.status === "loaded" ? (
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Company</TableCell>
                  <TableCell>Slug</TableCell>
                  <TableCell>Business Type</TableCell>
                  <TableCell>Activation Status</TableCell>
                  <TableCell>Plan</TableCell>
                  <TableCell>Expires At</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {state.companies.map((company) => (
                  <TableRow key={company.companySlug}>
                    <TableCell>{company.companyName}</TableCell>
                    <TableCell>{company.companySlug}</TableCell>
                    <TableCell>{company.businessType}</TableCell>
                    <TableCell>{company.activationStatus}</TableCell>
                    <TableCell>{company.planType}</TableCell>
                    <TableCell>{new Date(company.expiresAt).toLocaleString("en-GB", { timeZone: "UTC" })}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          ) : null}

          <LogoutButton />
        </Stack>
      </Paper>
    </Box>
  );
}
