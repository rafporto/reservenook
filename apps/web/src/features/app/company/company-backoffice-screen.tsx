"use client";

import { useEffect, useRef, useState } from "react";
import { Alert, Box, Paper, Stack, Typography } from "@mui/material";
import { useRouter } from "next/navigation";
import { LogoutButton } from "@/components/app/logout-button";
import { loadCompanyBackoffice, saveCompanyBackofficeSection } from "@/features/app/company/company-backoffice-client";
import { buildDrafts } from "@/features/app/company/company-backoffice-drafts";
import { AppointmentPanels } from "@/features/app/company/company-appointment-panels";
import { ApiMessageResponse, CompanyBackofficeData, Drafts, Feedback, State } from "@/features/app/company/company-backoffice-types";
import { BookingAuditPanel, ContactAndBookingPanels } from "@/features/app/company/company-booking-panels";
import { ClassManagementPanels } from "@/features/app/company/company-class-panels";
import { LocalizationAndNotificationPanels } from "@/features/app/company/company-localization-panels";
import { CompanyOverviewPanels, ConfigurationAreasPanel } from "@/features/app/company/company-overview-panels";
import { ProfileAndBrandingPanels } from "@/features/app/company/company-profile-panels";
import { CustomerQuestionsAndWidgetPanels } from "@/features/app/company/company-question-widget-panels";
import { SchedulingPanels } from "@/features/app/company/company-scheduling-panels";
import { StaffPanels } from "@/features/app/company/company-staff-panels";

type Props = { slug: string };

export function CompanyBackofficeScreen({ slug }: Props) {
  const router = useRouter();
  const routerRef = useRef(router);
  const [state, setState] = useState<State>({ status: "loading" });
  const [drafts, setDrafts] = useState<Drafts | null>(null);
  const [feedback, setFeedback] = useState<Feedback>({});
  const [saving, setSaving] = useState<string | null>(null);

  routerRef.current = router;

  function setSectionFeedback(section: string, next: Feedback[string]) {
    setFeedback((current) => ({ ...current, [section]: next }));
  }

  function hydrate(data: CompanyBackofficeData) {
    setDrafts(buildDrafts(data));
  }

  function applyData(data: CompanyBackofficeData) {
    setState({ status: "loaded", data });
    hydrate(data);
  }

  function updateDrafts(updater: (current: Drafts) => Drafts) {
    setDrafts((current) => (current ? updater(current) : current));
  }

  useEffect(() => {
    let active = true;
    (async () => {
      const result = await loadCompanyBackoffice(slug);
      if (!active) {
        return;
      }
      if (result.status === "unauthorized") {
        routerRef.current.replace("/en/login");
        return;
      }
      if (result.status === "loaded") {
        hydrate(result.data);
        setState({ status: "loaded", data: result.data });
        return;
      }
      if (result.status === "forbidden") {
        setState({ status: "forbidden" });
        return;
      }
      setState({ status: "error", message: result.message });
    })();
    return () => {
      active = false;
    };
  }, [slug]);

  async function saveSection<TResponse extends ApiMessageResponse>(
    section: string,
    endpoint: string,
    method: "PUT" | "POST",
    body: Record<string, unknown>,
    apply: (payload: TResponse) => void
  ) {
    setSectionFeedback(section, null);
    setSaving(section);
    const result = await saveCompanyBackofficeSection<TResponse>(endpoint, method, body);
    if (result.status === "unauthorized") {
      routerRef.current.replace("/en/login");
      setSaving(null);
      return;
    }
    if (result.status === "forbidden") {
      setState({ status: "forbidden" });
      setSaving(null);
      return;
    }
    if (result.status === "error") {
      setSectionFeedback(section, { type: "error", message: result.message });
      setSaving(null);
      return;
    }
    const payload = result.payload;
    if (payload == null) {
      setSectionFeedback(section, { type: "error", message: "The changes could not be saved." });
      setSaving(null);
      return;
    }
    apply(payload);
    setSectionFeedback(section, { type: "success", message: payload.message ?? "Changes saved." });
    setSaving(null);
  }

  if (state.status === "loading") {
    return (
      <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
        <Paper sx={{ maxWidth: 1280, mx: "auto", p: 4 }}>
          <Stack spacing={2}>
            <Typography variant="h3">Company Configuration</Typography>
            <Typography color="text.secondary">Loading company configuration dashboard...</Typography>
          </Stack>
        </Paper>
      </Box>
    );
  }

  if (state.status === "forbidden") {
    return (
      <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
        <Paper sx={{ maxWidth: 960, mx: "auto", p: 4 }}>
          <Stack spacing={2}>
            <Typography variant="h3">Company Configuration</Typography>
            <Alert severity="error">Access denied for this company scope.</Alert>
            <LogoutButton />
          </Stack>
        </Paper>
      </Box>
    );
  }

  if (state.status === "error") {
    return (
      <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
        <Paper sx={{ maxWidth: 960, mx: "auto", p: 4 }}>
          <Stack spacing={2}>
            <Typography variant="h3">Company Configuration</Typography>
            <Alert severity="error">{state.message}</Alert>
            <LogoutButton />
          </Stack>
        </Paper>
      </Box>
    );
  }

  if (drafts == null) {
    return null;
  }

  const panelProps = {
    slug,
    data: state.data,
    drafts,
    feedback,
    saving,
    updateDrafts,
    saveSection,
    setSectionFeedback,
    applyData
  };

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper sx={{ maxWidth: 1280, mx: "auto", p: { xs: 3, md: 5 } }}>
        <Stack spacing={4}>
          <Stack spacing={1}>
            <Typography variant="h3">{state.data.company.companyName}</Typography>
            <Typography color="text.secondary">
              Shared company configuration dashboard for secure tenant-scoped settings, staffing, and booking setup.
            </Typography>
          </Stack>

          <CompanyOverviewPanels data={state.data} />
          <ProfileAndBrandingPanels {...panelProps} />
          <LocalizationAndNotificationPanels {...panelProps} />
          <ContactAndBookingPanels {...panelProps} />
          <BookingAuditPanel data={state.data} />
          <SchedulingPanels {...panelProps} />
          <AppointmentPanels {...panelProps} />
          <ClassManagementPanels {...panelProps} />
          <StaffPanels {...panelProps} />
          <CustomerQuestionsAndWidgetPanels {...panelProps} />
          <ConfigurationAreasPanel data={state.data} />
          <LogoutButton />
        </Stack>
      </Paper>
    </Box>
  );
}
