"use client";

import { Alert, Button, Checkbox, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import {
  CompanyBackofficeData,
  days,
  formatUtcDateTime,
  restaurantReservationStatuses
} from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

function parseIdList(value: string) {
  return value.split(",").map((item) => item.trim()).filter(Boolean).map((item) => Number(item)).filter(Number.isInteger);
}

export function RestaurantManagementPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Dining Areas</Typography>
            {feedback.diningAreas ? <Alert severity={feedback.diningAreas.type}>{feedback.diningAreas.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const displayOrder = Number(drafts.diningAreaCreate.displayOrder);
              if (!drafts.diningAreaCreate.name.trim() || !Number.isInteger(displayOrder)) {
                setSectionFeedback("diningAreas", { type: "error", message: "Dining areas require a name and display order." });
                return;
              }
              void saveSection<{ message: string; diningArea: CompanyBackofficeData["diningAreas"][number]; }>(
                "diningAreas",
                `/api/app/company/${slug}/dining-areas`,
                "POST",
                { name: drafts.diningAreaCreate.name.trim(), displayOrder, active: drafts.diningAreaCreate.active },
                (payload) => applyData({ ...data, diningAreas: [...data.diningAreas, payload.diningArea].sort((a, b) => a.displayOrder - b.displayOrder) })
              );
            }}>
              <TextField label="Area name" value={drafts.diningAreaCreate.name} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaCreate: { ...current.diningAreaCreate, name: event.target.value } }))} fullWidth />
              <TextField label="Display order" value={drafts.diningAreaCreate.displayOrder} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaCreate: { ...current.diningAreaCreate, displayOrder: event.target.value } }))} fullWidth />
              <FormControlLabel control={<Checkbox checked={drafts.diningAreaCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaCreate: { ...current.diningAreaCreate, active: event.target.checked } }))} />} label="Area active" />
              <Button type="submit" variant="contained" disabled={saving === "diningAreas"}>{saving === "diningAreas" ? "Saving dining area..." : "Create dining area"}</Button>
            </Stack>
            {data.diningAreas.map((area) => (
              <Paper key={area.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{area.name}</Typography>
                  <TextField label={`Area name ${area.id}`} value={drafts.diningAreaUpdate[area.id]?.name ?? area.name} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaUpdate: { ...current.diningAreaUpdate, [area.id]: { ...current.diningAreaUpdate[area.id], name: event.target.value } } }))} fullWidth />
                  <TextField label={`Display order ${area.id}`} value={drafts.diningAreaUpdate[area.id]?.displayOrder ?? String(area.displayOrder)} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaUpdate: { ...current.diningAreaUpdate, [area.id]: { ...current.diningAreaUpdate[area.id], displayOrder: event.target.value } } }))} fullWidth />
                  <FormControlLabel control={<Checkbox checked={drafts.diningAreaUpdate[area.id]?.active ?? area.active} onChange={(event) => updateDrafts((current) => ({ ...current, diningAreaUpdate: { ...current.diningAreaUpdate, [area.id]: { ...current.diningAreaUpdate[area.id], active: event.target.checked } } }))} />} label="Area active" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.diningAreaUpdate[area.id];
                    const displayOrder = Number(draft?.displayOrder ?? area.displayOrder);
                    if (!draft?.name.trim() || !Number.isInteger(displayOrder)) {
                      setSectionFeedback("diningAreas", { type: "error", message: "Dining areas require a name and display order." });
                      return;
                    }
                    void saveSection<{ message: string; diningArea: CompanyBackofficeData["diningAreas"][number]; }>(
                      "diningAreas",
                      `/api/app/company/${slug}/dining-areas/${area.id}`,
                      "PUT",
                      { name: draft.name.trim(), displayOrder, active: draft.active },
                      (payload) => applyData({ ...data, diningAreas: data.diningAreas.map((item) => item.id === area.id ? payload.diningArea : item).sort((a, b) => a.displayOrder - b.displayOrder) })
                    );
                  }}>Save dining area</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Restaurant Tables</Typography>
            {feedback.restaurantTables ? <Alert severity={feedback.restaurantTables.type}>{feedback.restaurantTables.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const minPartySize = Number(drafts.restaurantTableCreate.minPartySize);
              const maxPartySize = Number(drafts.restaurantTableCreate.maxPartySize);
              if (!drafts.restaurantTableCreate.diningAreaId || !drafts.restaurantTableCreate.label.trim() || !Number.isInteger(minPartySize) || !Number.isInteger(maxPartySize)) {
                setSectionFeedback("restaurantTables", { type: "error", message: "Restaurant tables require an area, label, and party-size range." });
                return;
              }
              void saveSection<{ message: string; restaurantTable: CompanyBackofficeData["restaurantTables"][number]; }>(
                "restaurantTables",
                `/api/app/company/${slug}/restaurant-tables`,
                "POST",
                { diningAreaId: Number(drafts.restaurantTableCreate.diningAreaId), label: drafts.restaurantTableCreate.label.trim(), minPartySize, maxPartySize, active: drafts.restaurantTableCreate.active },
                (payload) => applyData({ ...data, restaurantTables: [...data.restaurantTables, payload.restaurantTable] })
              );
            }}>
              <TextField label="Dining area" value={drafts.restaurantTableCreate.diningAreaId} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCreate: { ...current.restaurantTableCreate, diningAreaId: event.target.value } }))} select fullWidth>
                {data.diningAreas.map((area) => <MenuItem key={area.id} value={String(area.id)}>{area.name}</MenuItem>)}
              </TextField>
              <TextField label="Table label" value={drafts.restaurantTableCreate.label} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCreate: { ...current.restaurantTableCreate, label: event.target.value } }))} fullWidth />
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Min party size" value={drafts.restaurantTableCreate.minPartySize} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCreate: { ...current.restaurantTableCreate, minPartySize: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Max party size" value={drafts.restaurantTableCreate.maxPartySize} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCreate: { ...current.restaurantTableCreate, maxPartySize: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <FormControlLabel control={<Checkbox checked={drafts.restaurantTableCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCreate: { ...current.restaurantTableCreate, active: event.target.checked } }))} />} label="Table active" />
              <Button type="submit" variant="contained" disabled={saving === "restaurantTables"}>{saving === "restaurantTables" ? "Saving table..." : "Create table"}</Button>
            </Stack>
            {data.restaurantTables.map((table) => (
              <Paper key={table.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{table.label}</Typography>
                  <Typography color="text.secondary">{table.diningAreaName}</Typography>
                  <TextField label={`Area ${table.id}`} value={drafts.restaurantTableUpdate[table.id]?.diningAreaId ?? String(table.diningAreaId)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableUpdate: { ...current.restaurantTableUpdate, [table.id]: { ...current.restaurantTableUpdate[table.id], diningAreaId: event.target.value } } }))} select fullWidth>
                    {data.diningAreas.map((area) => <MenuItem key={area.id} value={String(area.id)}>{area.name}</MenuItem>)}
                  </TextField>
                  <TextField label={`Label ${table.id}`} value={drafts.restaurantTableUpdate[table.id]?.label ?? table.label} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableUpdate: { ...current.restaurantTableUpdate, [table.id]: { ...current.restaurantTableUpdate[table.id], label: event.target.value } } }))} fullWidth />
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Min ${table.id}`} value={drafts.restaurantTableUpdate[table.id]?.minPartySize ?? String(table.minPartySize)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableUpdate: { ...current.restaurantTableUpdate, [table.id]: { ...current.restaurantTableUpdate[table.id], minPartySize: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Max ${table.id}`} value={drafts.restaurantTableUpdate[table.id]?.maxPartySize ?? String(table.maxPartySize)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableUpdate: { ...current.restaurantTableUpdate, [table.id]: { ...current.restaurantTableUpdate[table.id], maxPartySize: event.target.value } } }))} fullWidth />
                    </Grid>
                  </Grid>
                  <FormControlLabel control={<Checkbox checked={drafts.restaurantTableUpdate[table.id]?.active ?? table.active} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableUpdate: { ...current.restaurantTableUpdate, [table.id]: { ...current.restaurantTableUpdate[table.id], active: event.target.checked } } }))} />} label="Table active" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.restaurantTableUpdate[table.id];
                    const minPartySize = Number(draft?.minPartySize ?? table.minPartySize);
                    const maxPartySize = Number(draft?.maxPartySize ?? table.maxPartySize);
                    if (!draft?.diningAreaId || !draft.label.trim() || !Number.isInteger(minPartySize) || !Number.isInteger(maxPartySize)) {
                      setSectionFeedback("restaurantTables", { type: "error", message: "Restaurant tables require an area, label, and party-size range." });
                      return;
                    }
                    void saveSection<{ message: string; restaurantTable: CompanyBackofficeData["restaurantTables"][number]; }>(
                      "restaurantTables",
                      `/api/app/company/${slug}/restaurant-tables/${table.id}`,
                      "PUT",
                      { diningAreaId: Number(draft.diningAreaId), label: draft.label.trim(), minPartySize, maxPartySize, active: draft.active },
                      (payload) => applyData({ ...data, restaurantTables: data.restaurantTables.map((item) => item.id === table.id ? payload.restaurantTable : item) })
                    );
                  }}>Save table</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Combinable Tables</Typography>
            {feedback.restaurantTableCombinations ? <Alert severity={feedback.restaurantTableCombinations.type}>{feedback.restaurantTableCombinations.message}</Alert> : null}
            {drafts.restaurantTableCombinations.map((entry, index) => (
              <Grid container spacing={2} key={`${entry.primaryTableId}-${entry.secondaryTableId}-${index}`}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label={`Primary table ${index + 1}`} value={entry.primaryTableId} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCombinations: current.restaurantTableCombinations.map((item, itemIndex) => itemIndex === index ? { ...item, primaryTableId: event.target.value } : item) }))} select fullWidth>
                    {data.restaurantTables.map((table) => <MenuItem key={table.id} value={String(table.id)}>{table.label}</MenuItem>)}
                  </TextField>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label={`Secondary table ${index + 1}`} value={entry.secondaryTableId} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantTableCombinations: current.restaurantTableCombinations.map((item, itemIndex) => itemIndex === index ? { ...item, secondaryTableId: event.target.value } : item) }))} select fullWidth>
                    {data.restaurantTables.map((table) => <MenuItem key={table.id} value={String(table.id)}>{table.label}</MenuItem>)}
                  </TextField>
                </Grid>
              </Grid>
            ))}
            <Button variant="text" onClick={() => updateDrafts((current) => ({ ...current, restaurantTableCombinations: [...current.restaurantTableCombinations, { primaryTableId: "", secondaryTableId: "" }] }))}>Add combination</Button>
            <Button variant="contained" disabled={saving === "restaurantTableCombinations"} onClick={() => {
              const entries = drafts.restaurantTableCombinations.filter((entry) => entry.primaryTableId && entry.secondaryTableId).map((entry) => ({
                primaryTableId: Number(entry.primaryTableId),
                secondaryTableId: Number(entry.secondaryTableId)
              }));
              void saveSection<{ message: string; restaurantTableCombinations: CompanyBackofficeData["restaurantTableCombinations"]; }>(
                "restaurantTableCombinations",
                `/api/app/company/${slug}/restaurant-table-combinations`,
                "PUT",
                { entries },
                (payload) => applyData({ ...data, restaurantTableCombinations: payload.restaurantTableCombinations })
              );
            }}>{saving === "restaurantTableCombinations" ? "Saving combinations..." : "Save combinations"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Service Periods</Typography>
            {feedback.restaurantServicePeriods ? <Alert severity={feedback.restaurantServicePeriods.type}>{feedback.restaurantServicePeriods.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const slotIntervalMinutes = Number(drafts.restaurantServicePeriodCreate.slotIntervalMinutes);
              const reservationDurationMinutes = Number(drafts.restaurantServicePeriodCreate.reservationDurationMinutes);
              const minPartySize = Number(drafts.restaurantServicePeriodCreate.minPartySize);
              const maxPartySize = Number(drafts.restaurantServicePeriodCreate.maxPartySize);
              const bookingWindowDays = Number(drafts.restaurantServicePeriodCreate.bookingWindowDays);
              if (!drafts.restaurantServicePeriodCreate.name.trim() || !Number.isInteger(slotIntervalMinutes) || !Number.isInteger(reservationDurationMinutes) || !Number.isInteger(minPartySize) || !Number.isInteger(maxPartySize) || !Number.isInteger(bookingWindowDays)) {
                setSectionFeedback("restaurantServicePeriods", { type: "error", message: "Service periods require complete timing and party-size configuration." });
                return;
              }
              void saveSection<{ message: string; restaurantServicePeriod: CompanyBackofficeData["restaurantServicePeriods"][number]; }>(
                "restaurantServicePeriods",
                `/api/app/company/${slug}/restaurant-service-periods`,
                "POST",
                { ...drafts.restaurantServicePeriodCreate, slotIntervalMinutes, reservationDurationMinutes, minPartySize, maxPartySize, bookingWindowDays },
                (payload) => applyData({ ...data, restaurantServicePeriods: [...data.restaurantServicePeriods, payload.restaurantServicePeriod] })
              );
            }}>
              <TextField label="Period name" value={drafts.restaurantServicePeriodCreate.name} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, name: event.target.value } }))} fullWidth />
              <TextField label="Day" value={drafts.restaurantServicePeriodCreate.dayOfWeek} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, dayOfWeek: event.target.value } }))} select fullWidth>
                {days.map((day) => <MenuItem key={day} value={day}>{day}</MenuItem>)}
              </TextField>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Opens at" value={drafts.restaurantServicePeriodCreate.opensAt} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, opensAt: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Closes at" value={drafts.restaurantServicePeriodCreate.closesAt} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, closesAt: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Slot interval" value={drafts.restaurantServicePeriodCreate.slotIntervalMinutes} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, slotIntervalMinutes: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Reservation duration" value={drafts.restaurantServicePeriodCreate.reservationDurationMinutes} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, reservationDurationMinutes: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                  <TextField label="Booking window days" value={drafts.restaurantServicePeriodCreate.bookingWindowDays} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, bookingWindowDays: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Min party size" value={drafts.restaurantServicePeriodCreate.minPartySize} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, minPartySize: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Max party size" value={drafts.restaurantServicePeriodCreate.maxPartySize} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, maxPartySize: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <FormControlLabel control={<Checkbox checked={drafts.restaurantServicePeriodCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodCreate: { ...current.restaurantServicePeriodCreate, active: event.target.checked } }))} />} label="Service period active" />
              <Button type="submit" variant="contained" disabled={saving === "restaurantServicePeriods"}>{saving === "restaurantServicePeriods" ? "Saving service period..." : "Create service period"}</Button>
            </Stack>
            {data.restaurantServicePeriods.map((period) => (
              <Paper key={period.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{period.name}</Typography>
                  <Typography color="text.secondary">{period.dayOfWeek} · {period.opensAt}-{period.closesAt}</Typography>
                  <TextField label={`Period name ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.name ?? period.name} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], name: event.target.value } } }))} fullWidth />
                  <TextField label={`Day ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.dayOfWeek ?? period.dayOfWeek} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], dayOfWeek: event.target.value } } }))} select fullWidth>
                    {days.map((day) => <MenuItem key={day} value={day}>{day}</MenuItem>)}
                  </TextField>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Opens ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.opensAt ?? period.opensAt} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], opensAt: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Closes ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.closesAt ?? period.closesAt} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], closesAt: event.target.value } } }))} fullWidth />
                    </Grid>
                  </Grid>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Slot interval ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.slotIntervalMinutes ?? String(period.slotIntervalMinutes)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], slotIntervalMinutes: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Duration ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.reservationDurationMinutes ?? String(period.reservationDurationMinutes)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], reservationDurationMinutes: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 4 }}>
                      <TextField label={`Window ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.bookingWindowDays ?? String(period.bookingWindowDays)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], bookingWindowDays: event.target.value } } }))} fullWidth />
                    </Grid>
                  </Grid>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Min party ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.minPartySize ?? String(period.minPartySize)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], minPartySize: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Max party ${period.id}`} value={drafts.restaurantServicePeriodUpdate[period.id]?.maxPartySize ?? String(period.maxPartySize)} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], maxPartySize: event.target.value } } }))} fullWidth />
                    </Grid>
                  </Grid>
                  <FormControlLabel control={<Checkbox checked={drafts.restaurantServicePeriodUpdate[period.id]?.active ?? period.active} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantServicePeriodUpdate: { ...current.restaurantServicePeriodUpdate, [period.id]: { ...current.restaurantServicePeriodUpdate[period.id], active: event.target.checked } } }))} />} label="Service period active" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.restaurantServicePeriodUpdate[period.id];
                    const slotIntervalMinutes = Number(draft?.slotIntervalMinutes ?? period.slotIntervalMinutes);
                    const reservationDurationMinutes = Number(draft?.reservationDurationMinutes ?? period.reservationDurationMinutes);
                    const minPartySize = Number(draft?.minPartySize ?? period.minPartySize);
                    const maxPartySize = Number(draft?.maxPartySize ?? period.maxPartySize);
                    const bookingWindowDays = Number(draft?.bookingWindowDays ?? period.bookingWindowDays);
                    if (!draft?.name.trim() || !Number.isInteger(slotIntervalMinutes) || !Number.isInteger(reservationDurationMinutes) || !Number.isInteger(minPartySize) || !Number.isInteger(maxPartySize) || !Number.isInteger(bookingWindowDays)) {
                      setSectionFeedback("restaurantServicePeriods", { type: "error", message: "Service periods require complete timing and party-size configuration." });
                      return;
                    }
                    void saveSection<{ message: string; restaurantServicePeriod: CompanyBackofficeData["restaurantServicePeriods"][number]; }>(
                      "restaurantServicePeriods",
                      `/api/app/company/${slug}/restaurant-service-periods/${period.id}`,
                      "PUT",
                      { ...draft, slotIntervalMinutes, reservationDurationMinutes, minPartySize, maxPartySize, bookingWindowDays },
                      (payload) => applyData({ ...data, restaurantServicePeriods: data.restaurantServicePeriods.map((item) => item.id === period.id ? payload.restaurantServicePeriod : item) })
                    );
                  }}>Save service period</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Reservations And Floorbook</Typography>
            {feedback.restaurantReservations ? <Alert severity={feedback.restaurantReservations.type}>{feedback.restaurantReservations.message}</Alert> : null}
            {data.restaurantReservations.map((reservation) => (
              <Paper key={reservation.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{reservation.customerName} · party of {reservation.partySize}</Typography>
                  <Typography color="text.secondary">{reservation.customerEmail} · {reservation.servicePeriodName}</Typography>
                  <Typography color="text.secondary">{formatUtcDateTime(reservation.reservedAt)} · {reservation.tableLabels.join(", ") || "Unassigned"}</Typography>
                  <TextField label={`Reservation status ${reservation.id}`} value={drafts.restaurantReservationUpdate[reservation.id]?.status ?? reservation.status} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantReservationUpdate: { ...current.restaurantReservationUpdate, [reservation.id]: { ...(current.restaurantReservationUpdate[reservation.id] ?? { tableIdsText: reservation.tableIds.join(", ") }), status: event.target.value } } }))} select fullWidth>
                    {restaurantReservationStatuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                  <TextField label={`Table ids ${reservation.id}`} value={drafts.restaurantReservationUpdate[reservation.id]?.tableIdsText ?? reservation.tableIds.join(", ")} onChange={(event) => updateDrafts((current) => ({ ...current, restaurantReservationUpdate: { ...current.restaurantReservationUpdate, [reservation.id]: { ...(current.restaurantReservationUpdate[reservation.id] ?? { status: reservation.status }), tableIdsText: event.target.value } } }))} fullWidth helperText="Optional reseat assignment as comma-separated table ids." />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.restaurantReservationUpdate[reservation.id];
                    void saveSection<{ message: string; restaurantReservation: CompanyBackofficeData["restaurantReservations"][number]; }>(
                      "restaurantReservations",
                      `/api/app/company/${slug}/restaurant-reservations/${reservation.id}/status`,
                      "PUT",
                      { status: draft.status, tableIds: parseIdList(draft.tableIdsText) },
                      (payload) => applyData({ ...data, restaurantReservations: data.restaurantReservations.map((item) => item.id === reservation.id ? payload.restaurantReservation : item) })
                    );
                  }}>Update reservation</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
