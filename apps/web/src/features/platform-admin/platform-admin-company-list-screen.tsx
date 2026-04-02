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
import { CsrfTokenError, fetchCsrfToken } from "@/lib/security/csrf";

type PlatformAdminCompany = {
  companyName: string;
  companySlug: string;
  businessType: string;
  activationStatus: string;
  planType: string;
  expiresAt: string;
  legalHoldUntil: string | null;
};

type PlatformAdminCompanyListPayload = {
  companies: PlatformAdminCompany[];
};

type InactivityPolicy = {
  inactivityThresholdDays: number;
  deletionWarningLeadDays: number;
  updatedAt: string;
};

type AbusePolicy = {
  loginPairLimit: number;
  loginClientLimit: number;
  loginEmailLimit: number;
  publicWritePairLimit: number;
  publicWriteClientLimit: number;
  publicWriteEmailLimit: number;
  publicReadClientLimit: number;
  updatedAt: string;
};

type SecuritySummary = {
  auditEventsLast24Hours: number;
  rateLimitedEventsLast24Hours: number;
  loginFailuresLast24Hours: number;
  bookingEventsLast24Hours: number;
  lifecycleEventsLast24Hours: number;
};

type SecurityAuditRecord = {
  id: number;
  eventType: string;
  outcome: string;
  actorEmail: string | null;
  companySlug: string | null;
  targetEmail: string | null;
  details: string | null;
  createdAt: string;
};

type PlatformOperationsPayload = {
  summary: SecuritySummary;
  securityAudit: SecurityAuditRecord[];
};

export function PlatformAdminCompanyListScreen() {
  const router = useRouter();
  const [state, setState] = useState<
    | { status: "loading" }
    | {
        status: "loaded";
        companies: PlatformAdminCompany[];
        policy: InactivityPolicy;
        abusePolicy: AbusePolicy;
        operations: PlatformOperationsPayload;
      }
    | { status: "forbidden" }
    | { status: "error"; message: string }
  >({ status: "loading" });
  const [policyDraft, setPolicyDraft] = useState({
    inactivityThresholdDays: "",
    deletionWarningLeadDays: ""
  });
  const [policyFeedback, setPolicyFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [isSavingPolicy, setIsSavingPolicy] = useState(false);
  const [abuseDraft, setAbuseDraft] = useState({
    loginPairLimit: "",
    loginClientLimit: "",
    loginEmailLimit: "",
    publicWritePairLimit: "",
    publicWriteClientLimit: "",
    publicWriteEmailLimit: "",
    publicReadClientLimit: ""
  });
  const [abuseFeedback, setAbuseFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [isSavingAbusePolicy, setIsSavingAbusePolicy] = useState(false);
  const [legalHoldDrafts, setLegalHoldDrafts] = useState<Record<string, string>>({});
  const [retentionFeedback, setRetentionFeedback] = useState<Record<string, { type: "success" | "error"; message: string } | null>>({});
  const [savingRetentionSlug, setSavingRetentionSlug] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function loadPlatformAdminData() {
      try {
        const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
        const [companyResponse, policyResponse, abusePolicyResponse, operationsResponse] = await Promise.all([
          fetch(`${apiBaseUrl}/api/platform-admin/companies`, { credentials: "include" }),
          fetch(`${apiBaseUrl}/api/platform-admin/inactivity-policy`, { credentials: "include" }),
          fetch(`${apiBaseUrl}/api/platform-admin/abuse-policy`, { credentials: "include" }),
          fetch(`${apiBaseUrl}/api/platform-admin/operations-summary`, { credentials: "include" })
        ]);

        if (!isMounted) {
          return;
        }

        if (
          companyResponse.status === 401 ||
          policyResponse.status === 401 ||
          abusePolicyResponse.status === 401 ||
          operationsResponse.status === 401
        ) {
          router.replace("/en/login");
          return;
        }

        if (
          companyResponse.status === 403 ||
          policyResponse.status === 403 ||
          abusePolicyResponse.status === 403 ||
          operationsResponse.status === 403
        ) {
          setState({ status: "forbidden" });
          return;
        }

        if (!companyResponse.ok || !policyResponse.ok || !abusePolicyResponse.ok || !operationsResponse.ok) {
          setState({ status: "error", message: "The platform admin area could not be loaded." });
          return;
        }

        const companyPayload = (await companyResponse.json()) as PlatformAdminCompanyListPayload;
        const policyPayload = (await policyResponse.json()) as InactivityPolicy;
        const abusePolicyPayload = (await abusePolicyResponse.json()) as AbusePolicy;
        const operationsPayload = (await operationsResponse.json()) as PlatformOperationsPayload;
        setPolicyDraft({
          inactivityThresholdDays: String(policyPayload.inactivityThresholdDays),
          deletionWarningLeadDays: String(policyPayload.deletionWarningLeadDays)
        });
        setAbuseDraft({
          loginPairLimit: String(abusePolicyPayload.loginPairLimit),
          loginClientLimit: String(abusePolicyPayload.loginClientLimit),
          loginEmailLimit: String(abusePolicyPayload.loginEmailLimit),
          publicWritePairLimit: String(abusePolicyPayload.publicWritePairLimit),
          publicWriteClientLimit: String(abusePolicyPayload.publicWriteClientLimit),
          publicWriteEmailLimit: String(abusePolicyPayload.publicWriteEmailLimit),
          publicReadClientLimit: String(abusePolicyPayload.publicReadClientLimit)
        });
        setLegalHoldDrafts(
          Object.fromEntries(
            companyPayload.companies.map((company) => [
              company.companySlug,
              company.legalHoldUntil ? company.legalHoldUntil.slice(0, 16) : ""
            ])
          )
        );
        setState({
          status: "loaded",
          companies: companyPayload.companies,
          policy: policyPayload,
          abusePolicy: abusePolicyPayload,
          operations: operationsPayload
        });
      } catch {
        if (!isMounted) {
          return;
        }

        setState({ status: "error", message: "The platform admin area could not be loaded." });
      }
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

    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/platform-admin/inactivity-policy`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
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

      if (!response.ok || !payload || !("policy" in payload) || !payload.policy) {
        setPolicyFeedback({
          type: "error",
          message: payload?.message ?? "The inactivity policy could not be saved."
        });
        setIsSavingPolicy(false);
        return;
      }

      const policy = payload.policy;

      if (state.status === "loaded") {
        setState({
          status: "loaded",
          companies: state.companies,
          policy,
          abusePolicy: state.abusePolicy,
          operations: state.operations
        });
        setPolicyDraft({
          inactivityThresholdDays: String(policy.inactivityThresholdDays),
          deletionWarningLeadDays: String(policy.deletionWarningLeadDays)
        });
      }

      setPolicyFeedback({
        type: "success",
        message: payload.message ?? "Inactivity policy updated."
      });
    } catch (error) {
      if (error instanceof CsrfTokenError && error.status === 401) {
        router.replace("/en/login");
        return;
      }

      setPolicyFeedback({
        type: "error",
        message: "The inactivity policy could not be saved."
      });
    } finally {
      setIsSavingPolicy(false);
    }
  }

  async function handleAbusePolicySubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAbuseFeedback(null);
    const values = Object.fromEntries(Object.entries(abuseDraft).map(([key, value]) => [key, Number(value)])) as Record<string, number>;
    if (Object.values(values).some((value) => !Number.isInteger(value) || value < 1 || value > 500)) {
      setAbuseFeedback({ type: "error", message: "Use whole-number limits between 1 and 500." });
      return;
    }
    setIsSavingAbusePolicy(true);
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/platform-admin/abuse-policy`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify(values)
        }
      );
      const payload = (await response.json().catch(() => null)) as { message?: string; policy?: AbusePolicy } | null;
      if (response.status === 401) {
        router.replace("/en/login");
        return;
      }
      if (response.status === 403) {
        setState({ status: "forbidden" });
        return;
      }
      if (!response.ok || !payload?.policy) {
        setAbuseFeedback({ type: "error", message: payload?.message ?? "The abuse policy could not be saved." });
        return;
      }
      if (state.status === "loaded") {
        setState({ ...state, abusePolicy: payload.policy });
      }
      setAbuseFeedback({ type: "success", message: payload.message ?? "Abuse prevention policy updated." });
    } catch (error) {
      if (error instanceof CsrfTokenError && error.status === 401) {
        router.replace("/en/login");
        return;
      }
      setAbuseFeedback({ type: "error", message: "The abuse policy could not be saved." });
    } finally {
      setIsSavingAbusePolicy(false);
    }
  }

  async function handleRetentionSave(companySlug: string) {
    setRetentionFeedback((current) => ({ ...current, [companySlug]: null }));
    setSavingRetentionSlug(companySlug);
    try {
      const csrfToken = await fetchCsrfToken();
      const legalHoldUntil = legalHoldDrafts[companySlug]?.trim() ? new Date(legalHoldDrafts[companySlug]).toISOString() : null;
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/platform-admin/companies/${companySlug}/retention`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify({ legalHoldUntil })
        }
      );
      const payload = (await response.json().catch(() => null)) as { message?: string; legalHoldUntil?: string | null } | null;
      if (response.status === 401) {
        router.replace("/en/login");
        return;
      }
      if (response.status === 403) {
        setState({ status: "forbidden" });
        return;
      }
      if (!response.ok) {
        setRetentionFeedback((current) => ({
          ...current,
          [companySlug]: { type: "error", message: payload?.message ?? "The legal hold could not be saved." }
        }));
        return;
      }
      if (state.status === "loaded") {
        setState({
          ...state,
          companies: state.companies.map((company) =>
            company.companySlug === companySlug ? { ...company, legalHoldUntil: payload?.legalHoldUntil ?? null } : company
          )
        });
      }
      setRetentionFeedback((current) => ({
        ...current,
        [companySlug]: { type: "success", message: payload?.message ?? "Company legal hold updated." }
      }));
    } catch (error) {
      if (error instanceof CsrfTokenError && error.status === 401) {
        router.replace("/en/login");
        return;
      }
      setRetentionFeedback((current) => ({
        ...current,
        [companySlug]: { type: "error", message: "The legal hold could not be saved." }
      }));
    } finally {
      setSavingRetentionSlug(null);
    }
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

              <Paper variant="outlined" sx={{ p: 3 }}>
                <Stack spacing={2} component="form" onSubmit={handleAbusePolicySubmit}>
                  <Stack spacing={1}>
                    <Typography variant="h5" component="h2">
                      Abuse Prevention Policy
                    </Typography>
                    <Typography color="text.secondary">
                      Tune the shared burst thresholds used for login, password recovery, activation resend, booking, and availability requests.
                    </Typography>
                    <Typography color="text.secondary">
                      Last updated: {new Date(state.abusePolicy.updatedAt).toLocaleString("en-GB", { timeZone: "UTC" })}
                    </Typography>
                  </Stack>

                  {abuseFeedback ? <Alert severity={abuseFeedback.type}>{abuseFeedback.message}</Alert> : null}

                  <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                    <TextField label="Login pair limit" type="number" value={abuseDraft.loginPairLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, loginPairLimit: event.target.value }))} fullWidth />
                    <TextField label="Login client limit" type="number" value={abuseDraft.loginClientLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, loginClientLimit: event.target.value }))} fullWidth />
                    <TextField label="Login email limit" type="number" value={abuseDraft.loginEmailLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, loginEmailLimit: event.target.value }))} fullWidth />
                  </Stack>

                  <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                    <TextField label="Public write pair limit" type="number" value={abuseDraft.publicWritePairLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, publicWritePairLimit: event.target.value }))} fullWidth />
                    <TextField label="Public write client limit" type="number" value={abuseDraft.publicWriteClientLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, publicWriteClientLimit: event.target.value }))} fullWidth />
                    <TextField label="Public write email limit" type="number" value={abuseDraft.publicWriteEmailLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, publicWriteEmailLimit: event.target.value }))} fullWidth />
                    <TextField label="Public read client limit" type="number" value={abuseDraft.publicReadClientLimit} onChange={(event) => setAbuseDraft((current) => ({ ...current, publicReadClientLimit: event.target.value }))} fullWidth />
                  </Stack>

                  <Box>
                    <Button type="submit" variant="contained" disabled={isSavingAbusePolicy}>
                      {isSavingAbusePolicy ? "Saving abuse policy..." : "Save abuse policy"}
                    </Button>
                  </Box>
                </Stack>
              </Paper>

              <Paper variant="outlined" sx={{ p: 3 }}>
                <Stack spacing={2}>
                  <Typography variant="h5" component="h2">Operational Monitoring</Typography>
                  <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                    <Typography>Audit events (24h): {state.operations.summary.auditEventsLast24Hours}</Typography>
                    <Typography>Rate-limited events (24h): {state.operations.summary.rateLimitedEventsLast24Hours}</Typography>
                    <Typography>Login failures (24h): {state.operations.summary.loginFailuresLast24Hours}</Typography>
                    <Typography>Booking events (24h): {state.operations.summary.bookingEventsLast24Hours}</Typography>
                    <Typography>Lifecycle events (24h): {state.operations.summary.lifecycleEventsLast24Hours}</Typography>
                  </Stack>
                  <Table size="small" aria-label="Recent security audit">
                    <TableHead>
                      <TableRow>
                        <TableCell>Event</TableCell>
                        <TableCell>Outcome</TableCell>
                        <TableCell>Actor</TableCell>
                        <TableCell>Company</TableCell>
                        <TableCell>Created At</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {state.operations.securityAudit.map((event) => (
                        <TableRow key={event.id}>
                          <TableCell>{event.eventType}</TableCell>
                          <TableCell>{event.outcome}</TableCell>
                          <TableCell>{event.actorEmail ?? "system"}</TableCell>
                          <TableCell>{event.companySlug ?? "global"}</TableCell>
                          <TableCell>{new Date(event.createdAt).toLocaleString("en-GB", { timeZone: "UTC" })}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
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
                    <TableCell>Legal Hold</TableCell>
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
                      <TableCell>
                        <Stack spacing={1}>
                          <TextField
                            type="datetime-local"
                            size="small"
                            value={legalHoldDrafts[company.companySlug] ?? ""}
                            onChange={(event) => {
                              setLegalHoldDrafts((current) => ({
                                ...current,
                                [company.companySlug]: event.target.value
                              }));
                            }}
                          />
                          <Button size="small" variant="outlined" onClick={() => void handleRetentionSave(company.companySlug)} disabled={savingRetentionSlug === company.companySlug}>
                            {savingRetentionSlug === company.companySlug ? "Saving..." : "Save hold"}
                          </Button>
                          {retentionFeedback[company.companySlug] ? (
                            <Alert severity={retentionFeedback[company.companySlug]?.type}>
                              {retentionFeedback[company.companySlug]?.message}
                            </Alert>
                          ) : null}
                        </Stack>
                      </TableCell>
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
