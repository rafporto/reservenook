"use client";

import { Alert, Button, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { bookingStatuses, emailPattern, formatUtcDateTime, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function ContactAndBookingPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Customer Contacts</Typography>
            {feedback.contacts ? <Alert severity={feedback.contacts.type}>{feedback.contacts.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              if (!drafts.contactCreate.fullName.trim() || !emailPattern.test(drafts.contactCreate.email)) {
                setSectionFeedback("contacts", { type: "error", message: "Contact full name and email must be valid." });
                return;
              }
              void saveSection<{
                message: string;
                customerContact: CompanyBackofficeData["customerContacts"][number];
              }>("contacts", `/api/app/company/${slug}/customer-contacts`, "POST", {
                fullName: drafts.contactCreate.fullName.trim(),
                email: drafts.contactCreate.email.trim(),
                phone: drafts.contactCreate.phone.trim() || null,
                preferredLanguage: drafts.contactCreate.preferredLanguage.trim() || null,
                notes: drafts.contactCreate.notes.trim() || null
              }, (payload) => applyData({ ...data, customerContacts: [...data.customerContacts, payload.customerContact] }));
            }}>
              <TextField label="Contact full name" value={drafts.contactCreate.fullName} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, fullName: event.target.value } }))} fullWidth />
              <TextField label="Contact email" value={drafts.contactCreate.email} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, email: event.target.value } }))} fullWidth />
              <TextField label="Contact phone" value={drafts.contactCreate.phone} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, phone: event.target.value } }))} fullWidth />
              <TextField label="Preferred language" value={drafts.contactCreate.preferredLanguage} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, preferredLanguage: event.target.value } }))} select fullWidth>
                {data.localization.supportedLanguages.map((language) => <MenuItem key={language} value={language}>{language}</MenuItem>)}
              </TextField>
              <TextField label="Contact notes" value={drafts.contactCreate.notes} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, notes: event.target.value } }))} multiline minRows={3} fullWidth />
              <Button type="submit" variant="contained">Create customer contact</Button>
            </Stack>
            {data.customerContacts.map((contact) => (
              <Paper key={contact.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={2}>
                  <Typography variant="subtitle1">{contact.fullName}</Typography>
                  <TextField label={`Full name ${contact.id}`} value={drafts.contactUpdate[contact.id]?.fullName ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, contactUpdate: { ...current.contactUpdate, [contact.id]: { ...current.contactUpdate[contact.id], fullName: event.target.value } } }))} fullWidth />
                  <TextField label={`Email ${contact.id}`} value={drafts.contactUpdate[contact.id]?.email ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, contactUpdate: { ...current.contactUpdate, [contact.id]: { ...current.contactUpdate[contact.id], email: event.target.value } } }))} fullWidth />
                  <TextField label={`Phone ${contact.id}`} value={drafts.contactUpdate[contact.id]?.phone ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, contactUpdate: { ...current.contactUpdate, [contact.id]: { ...current.contactUpdate[contact.id], phone: event.target.value } } }))} fullWidth />
                  <TextField label={`Language ${contact.id}`} value={drafts.contactUpdate[contact.id]?.preferredLanguage ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, contactUpdate: { ...current.contactUpdate, [contact.id]: { ...current.contactUpdate[contact.id], preferredLanguage: event.target.value } } }))} select fullWidth>
                    {data.localization.supportedLanguages.map((language) => <MenuItem key={language} value={language}>{language}</MenuItem>)}
                  </TextField>
                  <TextField label={`Notes ${contact.id}`} value={drafts.contactUpdate[contact.id]?.notes ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, contactUpdate: { ...current.contactUpdate, [contact.id]: { ...current.contactUpdate[contact.id], notes: event.target.value } } }))} multiline minRows={2} fullWidth />
                  <Button variant="outlined" onClick={() => {
                    const contactDraft = drafts.contactUpdate[contact.id];
                    if (!contactDraft?.fullName.trim() || !emailPattern.test(contactDraft.email)) {
                      setSectionFeedback("contacts", { type: "error", message: "Contact full name and email must be valid." });
                      return;
                    }
                    void saveSection<{
                      message: string;
                      customerContact: CompanyBackofficeData["customerContacts"][number];
                    }>("contacts", `/api/app/company/${slug}/customer-contacts/${contact.id}`, "PUT", {
                      fullName: contactDraft.fullName.trim(),
                      email: contactDraft.email.trim(),
                      phone: contactDraft.phone.trim() || null,
                      preferredLanguage: contactDraft.preferredLanguage.trim() || null,
                      notes: contactDraft.notes.trim() || null
                    }, (payload) => applyData({
                      ...data,
                      customerContacts: data.customerContacts.map((item) => item.id === contact.id ? payload.customerContact : item)
                    }));
                  }}>Save contact</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Booking History</Typography>
            {feedback.bookings ? <Alert severity={feedback.bookings.type}>{feedback.bookings.message}</Alert> : null}
            {data.bookings.length === 0 ? <Typography color="text.secondary">No bookings yet.</Typography> : null}
            {data.bookings.map((booking) => (
              <Paper key={booking.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{booking.customerName}</Typography>
                  <Typography color="text.secondary">{booking.customerEmail} · {booking.source}</Typography>
                  <Typography color="text.secondary">Created: {formatUtcDateTime(booking.createdAt)}</Typography>
                  <Typography color="text.secondary">Preferred date: {booking.preferredDate ?? "Not specified"}</Typography>
                  <Typography color="text.secondary">{booking.requestSummary ?? "No summary provided."}</Typography>
                  <TextField label={`Booking status ${booking.id}`} value={drafts.bookingUpdate[booking.id]?.status ?? booking.status} onChange={(event) => updateDrafts((current) => ({ ...current, bookingUpdate: { ...current.bookingUpdate, [booking.id]: { ...current.bookingUpdate[booking.id], status: event.target.value } } }))} select fullWidth>
                    {bookingStatuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                  <TextField label={`Booking note ${booking.id}`} value={drafts.bookingUpdate[booking.id]?.internalNote ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, bookingUpdate: { ...current.bookingUpdate, [booking.id]: { ...current.bookingUpdate[booking.id], internalNote: event.target.value } } }))} multiline minRows={2} fullWidth />
                  <Button variant="outlined" onClick={() => {
                    const bookingDraft = drafts.bookingUpdate[booking.id];
                    void saveSection<{
                      message: string;
                      booking: CompanyBackofficeData["bookings"][number];
                    }>("bookings", `/api/app/company/${slug}/bookings/${booking.id}/status`, "PUT", {
                      status: bookingDraft.status,
                      internalNote: bookingDraft.internalNote.trim() || null
                    }, (payload) => applyData({
                      ...data,
                      bookings: data.bookings.map((item) => item.id === booking.id ? payload.booking : item)
                    }));
                  }}>Update booking status</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}

export function BookingAuditPanel({ data }: Pick<SectionProps, "data">) {
  return (
    <Paper variant="outlined" sx={{ p: 3 }}>
      <Stack spacing={2}>
        <Typography variant="h5">Booking Audit Trail</Typography>
        {data.bookingAudit.length === 0 ? <Typography color="text.secondary">No booking audit events yet.</Typography> : null}
        {data.bookingAudit.map((entry) => (
          <Paper key={entry.id} variant="outlined" sx={{ p: 2 }}>
            <Stack spacing={0.5}>
              <Typography variant="subtitle2">{entry.actionType} · {entry.outcome}</Typography>
              <Typography color="text.secondary">Booking #{entry.bookingId} · {entry.actorEmail ?? "System"} · {formatUtcDateTime(entry.createdAt)}</Typography>
              <Typography color="text.secondary">{entry.details ?? "No additional details."}</Typography>
            </Stack>
          </Paper>
        ))}
      </Stack>
    </Paper>
  );
}
