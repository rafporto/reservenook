"use client";

import { Alert, Button, Checkbox, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { days, emailPattern, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function AppointmentPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Appointment Services</Typography>
            {feedback.appointmentServices ? <Alert severity={feedback.appointmentServices.type}>{feedback.appointmentServices.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const durationMinutes = Number(drafts.appointmentServiceCreate.durationMinutes);
              const bufferMinutes = Number(drafts.appointmentServiceCreate.bufferMinutes);
              if (!drafts.appointmentServiceCreate.name.trim() || !Number.isInteger(durationMinutes) || !Number.isInteger(bufferMinutes)) {
                setSectionFeedback("appointmentServices", { type: "error", message: "Appointment services require a name, duration, and buffer." });
                return;
              }
              void saveSection<{
                message: string;
                appointmentService: CompanyBackofficeData["appointmentServices"][number];
              }>("appointmentServices", `/api/app/company/${slug}/appointment-services`, "POST", {
                name: drafts.appointmentServiceCreate.name.trim(),
                description: drafts.appointmentServiceCreate.description.trim() || null,
                durationMinutes,
                bufferMinutes,
                priceLabel: drafts.appointmentServiceCreate.priceLabel.trim() || null,
                enabled: drafts.appointmentServiceCreate.enabled,
                autoConfirm: drafts.appointmentServiceCreate.autoConfirm
              }, (payload) => applyData({ ...data, appointmentServices: [...data.appointmentServices, payload.appointmentService] }));
            }}>
              <TextField label="Service name" value={drafts.appointmentServiceCreate.name} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, name: event.target.value } }))} fullWidth />
              <TextField label="Description" value={drafts.appointmentServiceCreate.description} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, description: event.target.value } }))} multiline minRows={2} fullWidth />
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Duration (min)" value={drafts.appointmentServiceCreate.durationMinutes} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, durationMinutes: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Buffer (min)" value={drafts.appointmentServiceCreate.bufferMinutes} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, bufferMinutes: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Price label" value={drafts.appointmentServiceCreate.priceLabel} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, priceLabel: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <FormControlLabel control={<Checkbox checked={drafts.appointmentServiceCreate.enabled} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, enabled: event.target.checked } }))} />} label="Service enabled" />
              <FormControlLabel control={<Checkbox checked={drafts.appointmentServiceCreate.autoConfirm} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentServiceCreate: { ...current.appointmentServiceCreate, autoConfirm: event.target.checked } }))} />} label="Auto confirm bookings" />
              <Button type="submit" variant="contained" disabled={saving === "appointmentServices"}>{saving === "appointmentServices" ? "Saving service..." : "Create appointment service"}</Button>
            </Stack>
            {data.appointmentServices.map((service) => (
              <Paper key={service.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{service.name}</Typography>
                  <TextField label={`Service name ${service.id}`} value={drafts.appointmentServiceUpdate[service.id]?.name ?? service.name} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], name: event.target.value } }
                  }))} fullWidth />
                  <TextField label={`Description ${service.id}`} value={drafts.appointmentServiceUpdate[service.id]?.description ?? ""} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], description: event.target.value } }
                  }))} multiline minRows={2} fullWidth />
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Duration ${service.id}`} value={drafts.appointmentServiceUpdate[service.id]?.durationMinutes ?? String(service.durationMinutes)} onChange={(event) => updateDrafts((current) => ({
                        ...current,
                        appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], durationMinutes: event.target.value } }
                      }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Buffer ${service.id}`} value={drafts.appointmentServiceUpdate[service.id]?.bufferMinutes ?? String(service.bufferMinutes)} onChange={(event) => updateDrafts((current) => ({
                        ...current,
                        appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], bufferMinutes: event.target.value } }
                      }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Price label ${service.id}`} value={drafts.appointmentServiceUpdate[service.id]?.priceLabel ?? ""} onChange={(event) => updateDrafts((current) => ({
                        ...current,
                        appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], priceLabel: event.target.value } }
                      }))} fullWidth />
                    </Grid>
                  </Grid>
                  <FormControlLabel control={<Checkbox checked={drafts.appointmentServiceUpdate[service.id]?.enabled ?? service.enabled} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], enabled: event.target.checked } }
                  }))} />} label="Service enabled" />
                  <FormControlLabel control={<Checkbox checked={drafts.appointmentServiceUpdate[service.id]?.autoConfirm ?? service.autoConfirm} onChange={(event) => updateDrafts((current) => ({
                    ...current,
                    appointmentServiceUpdate: { ...current.appointmentServiceUpdate, [service.id]: { ...current.appointmentServiceUpdate[service.id], autoConfirm: event.target.checked } }
                  }))} />} label="Auto confirm bookings" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.appointmentServiceUpdate[service.id];
                    const durationMinutes = Number(draft?.durationMinutes ?? service.durationMinutes);
                    const bufferMinutes = Number(draft?.bufferMinutes ?? service.bufferMinutes);
                    if (!draft?.name.trim() || !Number.isInteger(durationMinutes) || !Number.isInteger(bufferMinutes)) {
                      setSectionFeedback("appointmentServices", { type: "error", message: "Appointment services require a name, duration, and buffer." });
                      return;
                    }
                    void saveSection<{
                      message: string;
                      appointmentService: CompanyBackofficeData["appointmentServices"][number];
                    }>("appointmentServices", `/api/app/company/${slug}/appointment-services/${service.id}`, "PUT", {
                      name: draft.name.trim(),
                      description: draft.description.trim() || null,
                      durationMinutes,
                      bufferMinutes,
                      priceLabel: draft.priceLabel.trim() || null,
                      enabled: draft.enabled,
                      autoConfirm: draft.autoConfirm
                    }, (payload) => applyData({
                      ...data,
                      appointmentServices: data.appointmentServices.map((item) => item.id === service.id ? payload.appointmentService : item)
                    }));
                  }}>Save appointment service</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Providers And Availability</Typography>
            {feedback.appointmentProviders ? <Alert severity={feedback.appointmentProviders.type}>{feedback.appointmentProviders.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              if (!drafts.appointmentProviderCreate.displayName.trim()) {
                setSectionFeedback("appointmentProviders", { type: "error", message: "Providers require a display name." });
                return;
              }
              if (drafts.appointmentProviderCreate.email.trim() && !emailPattern.test(drafts.appointmentProviderCreate.email.trim())) {
                setSectionFeedback("appointmentProviders", { type: "error", message: "Provider email must be valid." });
                return;
              }
              void saveSection<{
                message: string;
                appointmentProvider: CompanyBackofficeData["appointmentProviders"][number];
              }>("appointmentProviders", `/api/app/company/${slug}/appointment-providers`, "POST", {
                linkedUserId: drafts.appointmentProviderCreate.linkedUserId ? Number(drafts.appointmentProviderCreate.linkedUserId) : null,
                displayName: drafts.appointmentProviderCreate.displayName.trim(),
                email: drafts.appointmentProviderCreate.email.trim() || null,
                active: drafts.appointmentProviderCreate.active
              }, (payload) => applyData({ ...data, appointmentProviders: [...data.appointmentProviders, payload.appointmentProvider] }));
            }}>
              <TextField label="Provider display name" value={drafts.appointmentProviderCreate.displayName} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentProviderCreate: { ...current.appointmentProviderCreate, displayName: event.target.value } }))} fullWidth />
              <TextField label="Linked staff user" value={drafts.appointmentProviderCreate.linkedUserId} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentProviderCreate: { ...current.appointmentProviderCreate, linkedUserId: event.target.value } }))} select fullWidth>
                <MenuItem value="">No linked user</MenuItem>
                {data.staffUsers.map((user) => (
                  <MenuItem key={user.userId ?? user.membershipId} value={String(user.userId ?? "")}>
                    {user.fullName ?? user.email} ({user.email})
                  </MenuItem>
                ))}
              </TextField>
              <TextField label="Provider email" value={drafts.appointmentProviderCreate.email} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentProviderCreate: { ...current.appointmentProviderCreate, email: event.target.value } }))} fullWidth />
              <FormControlLabel control={<Checkbox checked={drafts.appointmentProviderCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, appointmentProviderCreate: { ...current.appointmentProviderCreate, active: event.target.checked } }))} />} label="Provider active" />
              <Button type="submit" variant="contained" disabled={saving === "appointmentProviders"}>{saving === "appointmentProviders" ? "Saving provider..." : "Create provider"}</Button>
            </Stack>
            {data.appointmentProviders.map((provider) => {
              const schedule = data.providerSchedules.find((entry) => entry.providerId === provider.id);
              const availability = drafts.providerAvailabilityUpdate[provider.id] ?? schedule?.availability ?? [];
              return (
                <Paper key={provider.id} variant="outlined" sx={{ p: 2 }}>
                  <Stack spacing={1.5}>
                    <Typography variant="subtitle1">{provider.displayName}</Typography>
                    <TextField label={`Provider name ${provider.id}`} value={drafts.appointmentProviderUpdate[provider.id]?.displayName ?? provider.displayName} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      appointmentProviderUpdate: { ...current.appointmentProviderUpdate, [provider.id]: { ...current.appointmentProviderUpdate[provider.id], displayName: event.target.value } }
                    }))} fullWidth />
                    <TextField label={`Linked user ${provider.id}`} value={drafts.appointmentProviderUpdate[provider.id]?.linkedUserId ?? ""} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      appointmentProviderUpdate: { ...current.appointmentProviderUpdate, [provider.id]: { ...current.appointmentProviderUpdate[provider.id], linkedUserId: event.target.value } }
                    }))} select fullWidth>
                      <MenuItem value="">No linked user</MenuItem>
                      {data.staffUsers.map((user) => (
                        <MenuItem key={user.userId ?? user.membershipId} value={String(user.userId ?? "")}>
                          {user.fullName ?? user.email} ({user.email})
                        </MenuItem>
                      ))}
                    </TextField>
                    <TextField label={`Provider email ${provider.id}`} value={drafts.appointmentProviderUpdate[provider.id]?.email ?? ""} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      appointmentProviderUpdate: { ...current.appointmentProviderUpdate, [provider.id]: { ...current.appointmentProviderUpdate[provider.id], email: event.target.value } }
                    }))} fullWidth />
                    <FormControlLabel control={<Checkbox checked={drafts.appointmentProviderUpdate[provider.id]?.active ?? provider.active} onChange={(event) => updateDrafts((current) => ({
                      ...current,
                      appointmentProviderUpdate: { ...current.appointmentProviderUpdate, [provider.id]: { ...current.appointmentProviderUpdate[provider.id], active: event.target.checked } }
                    }))} />} label="Provider active" />
                    <Button variant="outlined" onClick={() => {
                      const draft = drafts.appointmentProviderUpdate[provider.id];
                      if (!draft?.displayName.trim()) {
                        setSectionFeedback("appointmentProviders", { type: "error", message: "Providers require a display name." });
                        return;
                      }
                      if (draft.email.trim() && !emailPattern.test(draft.email.trim())) {
                        setSectionFeedback("appointmentProviders", { type: "error", message: "Provider email must be valid." });
                        return;
                      }
                      void saveSection<{
                        message: string;
                        appointmentProvider: CompanyBackofficeData["appointmentProviders"][number];
                      }>("appointmentProviders", `/api/app/company/${slug}/appointment-providers/${provider.id}`, "PUT", {
                        linkedUserId: draft.linkedUserId ? Number(draft.linkedUserId) : null,
                        displayName: draft.displayName.trim(),
                        email: draft.email.trim() || null,
                        active: draft.active
                      }, (payload) => applyData({
                        ...data,
                        appointmentProviders: data.appointmentProviders.map((item) => item.id === provider.id ? payload.appointmentProvider : item)
                      }));
                    }}>Save provider</Button>
                    <Typography variant="subtitle2">Weekly availability</Typography>
                    {availability.map((entry, index) => (
                      <Grid key={`${provider.id}-${index}`} container spacing={2}>
                        <Grid size={{ xs: 12, md: 4 }}>
                          <TextField label="Day" value={entry.dayOfWeek} onChange={(event) => updateDrafts((current) => ({
                            ...current,
                            providerAvailabilityUpdate: { ...current.providerAvailabilityUpdate, [provider.id]: availability.map((item, itemIndex) => itemIndex === index ? { ...item, dayOfWeek: event.target.value } : item) }
                          }))} select fullWidth>
                            {days.map((day) => <MenuItem key={day} value={day}>{day}</MenuItem>)}
                          </TextField>
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <TextField label="Opens" type="time" value={entry.opensAt} onChange={(event) => updateDrafts((current) => ({
                            ...current,
                            providerAvailabilityUpdate: { ...current.providerAvailabilityUpdate, [provider.id]: availability.map((item, itemIndex) => itemIndex === index ? { ...item, opensAt: event.target.value } : item) }
                          }))} InputLabelProps={{ shrink: true }} fullWidth />
                        </Grid>
                        <Grid size={{ xs: 12, md: 3 }}>
                          <TextField label="Closes" type="time" value={entry.closesAt} onChange={(event) => updateDrafts((current) => ({
                            ...current,
                            providerAvailabilityUpdate: { ...current.providerAvailabilityUpdate, [provider.id]: availability.map((item, itemIndex) => itemIndex === index ? { ...item, closesAt: event.target.value } : item) }
                          }))} InputLabelProps={{ shrink: true }} fullWidth />
                        </Grid>
                        <Grid size={{ xs: 12, md: 2 }}>
                          <Button color="error" fullWidth onClick={() => updateDrafts((current) => ({
                            ...current,
                            providerAvailabilityUpdate: { ...current.providerAvailabilityUpdate, [provider.id]: availability.filter((_, itemIndex) => itemIndex !== index) }
                          }))}>Remove</Button>
                        </Grid>
                      </Grid>
                    ))}
                    <Stack direction="row" spacing={1}>
                      <Button onClick={() => updateDrafts((current) => ({
                        ...current,
                        providerAvailabilityUpdate: { ...current.providerAvailabilityUpdate, [provider.id]: [...availability, { dayOfWeek: "MONDAY", opensAt: "09:00", closesAt: "17:00", displayOrder: availability.length }] }
                      }))}>Add window</Button>
                      <Button variant="outlined" onClick={() => void saveSection<{
                        message: string;
                        providerSchedule: CompanyBackofficeData["providerSchedules"][number];
                      }>("appointmentProviders", `/api/app/company/${slug}/appointment-providers/${provider.id}/availability`, "PUT", {
                        entries: availability.map((entry, index) => ({ ...entry, displayOrder: index }))
                      }, (payload) => {
                        const nextSchedules = data.providerSchedules.some((item) => item.providerId === provider.id)
                          ? data.providerSchedules.map((item) => item.providerId === provider.id ? payload.providerSchedule : item)
                          : [...data.providerSchedules, payload.providerSchedule];
                        applyData({ ...data, providerSchedules: nextSchedules });
                      })}>Save availability</Button>
                    </Stack>
                  </Stack>
                </Paper>
              );
            })}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
