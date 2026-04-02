"use client";

import { Alert, Button, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { days, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function SchedulingPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            void saveSection<{
              message: string;
              businessHours: CompanyBackofficeData["businessHours"];
            }>("hours", `/api/app/company/${slug}/business-hours`, "PUT", {
              entries: drafts.businessHours.map((entry, index) => ({ ...entry, displayOrder: index }))
            }, (payload) => applyData({ ...data, businessHours: payload.businessHours }));
          }}>
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="h5">Business Hours</Typography>
              <Button onClick={() => updateDrafts((current) => ({
                ...current,
                businessHours: [...current.businessHours, { dayOfWeek: "MONDAY", opensAt: "09:00", closesAt: "17:00", displayOrder: current.businessHours.length }]
              }))}>Add hours</Button>
            </Stack>
            {feedback.hours ? <Alert severity={feedback.hours.type}>{feedback.hours.message}</Alert> : null}
            {drafts.businessHours.map((entry, index) => (
              <Paper key={`${entry.dayOfWeek}-${index}`} variant="outlined" sx={{ p: 2 }}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, md: 4 }}>
                    <TextField label="Day" value={entry.dayOfWeek} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      businessHours: current.businessHours.map((item, itemIndex) => itemIndex === index ? { ...item, dayOfWeek: event.target.value } : item)
                    }))} select fullWidth>
                      {days.map((day) => <MenuItem key={day} value={day}>{day}</MenuItem>)}
                    </TextField>
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField label="Opens" type="time" value={entry.opensAt} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      businessHours: current.businessHours.map((item, itemIndex) => itemIndex === index ? { ...item, opensAt: event.target.value } : item)
                    }))} InputLabelProps={{ shrink: true }} fullWidth />
                  </Grid>
                  <Grid size={{ xs: 12, md: 3 }}>
                    <TextField label="Closes" type="time" value={entry.closesAt} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      businessHours: current.businessHours.map((item, itemIndex) => itemIndex === index ? { ...item, closesAt: event.target.value } : item)
                    }))} InputLabelProps={{ shrink: true }} fullWidth />
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <Button color="error" fullWidth onClick={() => updateDrafts((current) => ({
                      ...current,
                      businessHours: current.businessHours.filter((_, itemIndex) => itemIndex !== index)
                    }))}>Remove</Button>
                  </Grid>
                </Grid>
              </Paper>
            ))}
            <Button type="submit" variant="contained" disabled={saving === "hours"}>{saving === "hours" ? "Saving hours..." : "Save business hours"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            void saveSection<{
              message: string;
              closureDates: CompanyBackofficeData["closureDates"];
            }>("closures", `/api/app/company/${slug}/closure-dates`, "PUT", {
              entries: drafts.closureDates.map((entry) => ({
                label: entry.label.trim() || null,
                startsOn: entry.startsOn,
                endsOn: entry.endsOn
              }))
            }, (payload) => applyData({ ...data, closureDates: payload.closureDates }));
          }}>
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="h5">Closure Dates</Typography>
              <Button onClick={() => updateDrafts((current) => ({
                ...current,
                closureDates: [...current.closureDates, { label: "", startsOn: "", endsOn: "" }]
              }))}>Add closure</Button>
            </Stack>
            {feedback.closures ? <Alert severity={feedback.closures.type}>{feedback.closures.message}</Alert> : null}
            {drafts.closureDates.map((entry, index) => (
              <Paper key={`${entry.startsOn}-${entry.endsOn}-${index}`} variant="outlined" sx={{ p: 2 }}>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12 }}>
                    <TextField label="Label" value={entry.label} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      closureDates: current.closureDates.map((item, itemIndex) => itemIndex === index ? { ...item, label: event.target.value } : item)
                    }))} fullWidth />
                  </Grid>
                  <Grid size={{ xs: 12, md: 5 }}>
                    <TextField label="Starts on" type="date" value={entry.startsOn} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      closureDates: current.closureDates.map((item, itemIndex) => itemIndex === index ? { ...item, startsOn: event.target.value } : item)
                    }))} InputLabelProps={{ shrink: true }} fullWidth />
                  </Grid>
                  <Grid size={{ xs: 12, md: 5 }}>
                    <TextField label="Ends on" type="date" value={entry.endsOn} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      closureDates: current.closureDates.map((item, itemIndex) => itemIndex === index ? { ...item, endsOn: event.target.value } : item)
                    }))} InputLabelProps={{ shrink: true }} fullWidth />
                  </Grid>
                  <Grid size={{ xs: 12, md: 2 }}>
                    <Button color="error" fullWidth onClick={() => updateDrafts((current) => ({
                      ...current,
                      closureDates: current.closureDates.filter((_, itemIndex) => itemIndex !== index)
                    }))}>Remove</Button>
                  </Grid>
                </Grid>
              </Paper>
            ))}
            <Button type="submit" variant="contained" disabled={saving === "closures"}>{saving === "closures" ? "Saving closure dates..." : "Save closure dates"}</Button>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
