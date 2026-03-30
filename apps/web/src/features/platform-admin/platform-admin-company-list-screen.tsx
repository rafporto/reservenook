"use client";

import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
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

type InactivityPolicy = {
  inactivityThresholdDays: number;
  deletionWarningLeadDays: number;
  updatedAt: string;
};

export function PlatformAdminCompanyListScreen() {
  const router = useRouter();
  const [state, setState] = useState<
    | { status: "loading" }
    | { status: "loaded"; companies: PlatformAdminCompany[]; policy: InactivityPolicy }
    | { status: "forbidden" }
    | { status: "error"; message: string }
  >({ status: "loading" });
  const [policyDraft, setPolicyDraft] = useState({
    inactivityThresholdDays: "",
    deletionWarningLeadDays: ""
  });
  const [policyFeedback, setPolicyFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [isSavingPolicy, setIsSavingPolicy] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadPlatformAdminData() {
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
      const [companyResponse, policyResponse] = await Promise.all([
        fetch(`${apiBaseUrl}/api/platform-admin/companies`, { credentials: "include" }),
        fetch(`${apiBaseUrl}/api/platform-admin/inactivity-policy`, { credentials: "include" })
      ]);

      if (!isMounted) {
        return;
      }

      if (companyResponse.status === 401 || policyResponse.status === 401) {
        router.replace("/en/login");
        return;
      }

      if (companyResponse.status === 403 || policyResponse.status === 403) {
        setState({ status: "forbidden" });
        return;
      }

      if (!companyResponse.ok || !policyResponse.ok) {
        setState({ status: "error", message: "The platform admin area could not be loaded." });
        return;
      }

      const companyPayload = (await companyResponse.json()) as PlatformAdminCompanyListPayload;
      const policyPayload = (await policyResponse.json()) as InactivityPolicy;
      setPolicyDraft({
        inactivityThresholdDays: String(policyPayload.inactivityThresholdDays),
        deletionWarningLeadDays: String(policyPayload.deletionWarningLeadDays)
      });
      setState({ status: "loaded", companies: companyPayload.companies, policy: policyPayload });
    }

    void loadPlatformAdminData();

    return () => {
      isMounted = false;
    };
  }, [router]);

  async function handlePolicySubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPolicyFeedback(null);

    const inactivityThresholdDays = Number(policyDraft.inactivityThresholdDays);
    const deletionWarningLeadDays = Number(policyDraft.deletionWarningLeadDays);

    if (
      !Number.isInteger(inactivityThresholdDays) ||
      !Number.isInteger(deletionWarningLeadDays) ||
      inactivityThresholdDays < 1 ||
      deletionWarningLeadDays < 1
    ) {
      setPolicyFeedback({ type: "error", message: "Use whole-day values greater than zero." });
      return;
    }

    if (deletionWarningLeadDays > inactivityThresholdDays) {
      setPolicyFeedback({
        type: "error",
        message: "Deletion warning lead time cannot be greater than the inactivity threshold."
      });
      return;
    }

    setIsSavingPolicy(true);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/platform-admin/inactivity-policy`,
      {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          inactivityThresholdDays,
          deletionWarningLeadDays
        })
      }
    );

    const payload = (await response.json().catch(() => null)) as
      | { message?: string; policy?: InactivityPolicy }
      | { message?: string }
      | null;

    if (response.status === 401) {
      router.replace("/en/login");
      setIsSavingPolicy(false);
      return;
    }

    if (response.status === 403) {
      setState({ status: "forbidden" });
      setIsSavingPolicy(false);
      return;
    }

    if (!response.ok || !("policy" in (payload ?? {}))) {
      setPolicyFeedback({
        type: "error",
        message: payload?.message ?? "The inactivity policy could not be saved."
      });
      setIsSavingPolicy(false);
      return;
    }

    if (state.status === "loaded") {
      setState({
        status: "loaded",
        companies: state.companies,
        policy: payload.policy
      });
      setPolicyDraft({
        inactivityThresholdDays: String(payload.policy.inactivityThresholdDays),
        deletionWarningLeadDays: String(payload.policy.deletionWarningLeadDays)
      });
    }

    setPolicyFeedback({
      type: "success",
      message: payload.message ?? "Inactivity policy updated."
    });
    setIsSavingPolicy(false);
  }

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper elevation={0} sx={{ maxWidth: 1120, mx: "auto", p: 5, border: "1px solid", borderColor: "divider" }}>
        <Stack spacing={3}>
          <Stack spacing={1}>
            <Typography variant="h3" component="h1">
              Platform Admin
            </Typography>
            <Typography color="text.secondary">
              Review registered companies and configure the inactivity lifecycle used for future deletion handling.
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
            <Stack spacing={4}>
              <Paper variant="outlined" sx={{ p: 3 }}>
                <Stack spacing={2} component="form" onSubmit={handlePolicySubmit}>
                  <Stack spacing={1}>
                    <Typography variant="h5" component="h2">
                      Inactivity Policy
                    </Typography>
                    <Typography color="text.secondary">
                      Define when a company becomes inactive and when the deletion warning must be sent.
                    </Typography>
                    <Typography color="text.secondary">
                      Last updated: {new Date(state.policy.updatedAt).toLocaleString("en-GB", { timeZone: "UTC" })}
                    </Typography>
                  </Stack>

                  {policyFeedback ? <Alert severity={policyFeedback.type}>{policyFeedback.message}</Alert> : null}

                  <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                    <TextField
                      label="Inactivity threshold (days)"
                      type="number"
                      value={policyDraft.inactivityThresholdDays}
                      onChange={(event) => {
                        setPolicyDraft((current) => ({
                          ...current,
                          inactivityThresholdDays: event.target.value
                        }));
                      }}
                      fullWidth
                    />
                    <TextField
                      label="Deletion warning lead time (days)"
                      type="number"
                      value={policyDraft.deletionWarningLeadDays}
                      onChange={(event) => {
                        setPolicyDraft((current) => ({
                          ...current,
                          deletionWarningLeadDays: event.target.value
                        }));
                      }}
                      fullWidth
                    />
                  </Stack>

                  <Box>
                    <Button type="submit" variant="contained" disabled={isSavingPolicy}>
                      {isSavingPolicy ? "Saving policy..." : "Save inactivity policy"}
                    </Button>
                  </Box>
                </Stack>
              </Paper>

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
            </Stack>
          ) : null}

          <LogoutButton />
        </Stack>
      </Paper>
    </Box>
  );
}
