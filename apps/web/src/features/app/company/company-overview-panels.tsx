"use client";

import { Grid, Paper, Stack, Typography } from "@mui/material";
import { formatUtcDateTime } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function CompanyOverviewPanels({ data }: Pick<SectionProps, "data">) {
  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, md: 7 }}>
        <Paper variant="outlined" sx={{ p: 3, height: "100%" }}>
          <Stack spacing={1}>
            <Typography variant="h5">Company Summary</Typography>
            <Typography>{data.company.companySlug}</Typography>
            <Typography color="text.secondary">{data.viewer.currentUserEmail} · {data.viewer.role}</Typography>
            <Typography color="text.secondary">{data.company.defaultLanguage} / {data.company.defaultLocale}</Typography>
          </Stack>
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, md: 5 }}>
        <Paper variant="outlined" sx={{ p: 3, height: "100%" }}>
          <Stack spacing={1}>
            <Typography variant="h5">Operational Snapshot</Typography>
            <Typography>Plan: {data.operations.planType}</Typography>
            <Typography>Staff users: {data.operations.staffCount}</Typography>
            <Typography>Company admins: {data.operations.adminCount}</Typography>
            <Typography>Last activity: {formatUtcDateTime(data.operations.lastActivityAt)}</Typography>
            <Typography>Deletion schedule: {formatUtcDateTime(data.operations.deletionScheduledAt)}</Typography>
            <Typography>Legal hold: {formatUtcDateTime(data.operations.legalHoldUntil)}</Typography>
          </Stack>
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, md: 6 }}>
        <Paper variant="outlined" sx={{ p: 3, height: "100%" }}>
          <Stack spacing={1}>
            <Typography variant="h5">Security Snapshot</Typography>
            <Typography>Audit events (24h): {data.securitySummary.auditEventsLast24Hours}</Typography>
            <Typography>Rate-limited events (24h): {data.securitySummary.rateLimitedEventsLast24Hours}</Typography>
            <Typography>Login failures (24h): {data.securitySummary.loginFailuresLast24Hours}</Typography>
            <Typography>Booking events (24h): {data.securitySummary.bookingEventsLast24Hours}</Typography>
            <Typography>Lifecycle events (24h): {data.securitySummary.lifecycleEventsLast24Hours}</Typography>
          </Stack>
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, md: 6 }}>
        <Paper variant="outlined" sx={{ p: 3, height: "100%" }}>
          <Stack spacing={1.5}>
            <Typography variant="h5">Recent Security Audit</Typography>
            {data.securityAudit.length === 0 ? (
              <Typography color="text.secondary">No security audit events yet.</Typography>
            ) : (
              data.securityAudit.slice(0, 6).map((event) => (
                <Stack key={event.id} spacing={0.25}>
                  <Typography>{event.eventType} · {event.outcome}</Typography>
                  <Typography color="text.secondary">
                    {event.actorEmail ?? "system"} · {formatUtcDateTime(event.createdAt)}
                  </Typography>
                </Stack>
              ))
            )}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}

export function ConfigurationAreasPanel({ data }: Pick<SectionProps, "data">) {
  return (
    <Stack spacing={2}>
      <Typography variant="h5">Shared Configuration Areas</Typography>
      <Grid container spacing={2}>
        {data.configurationAreas.map((area) => (
          <Grid key={area.key} size={{ xs: 12, md: 6, xl: 4 }}>
            <Paper variant="outlined" sx={{ p: 2, height: "100%" }}>
              <Typography variant="h6">{area.title}</Typography>
              <Typography color="text.secondary">{area.description}</Typography>
              <Typography color="text.secondary">Status: {area.status}</Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>
    </Stack>
  );
}
