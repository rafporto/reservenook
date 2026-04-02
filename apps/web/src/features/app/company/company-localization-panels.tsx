"use client";

import { Alert, Button, Checkbox, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { emailPattern, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function LocalizationAndNotificationPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, lg: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            void saveSection<{
              message: string;
              localization: CompanyBackofficeData["localization"];
              company: CompanyBackofficeData["company"];
            }>("localization", `/api/app/company/${slug}/localization`, "PUT", {
              defaultLanguage: drafts.localization.defaultLanguage,
              defaultLocale: drafts.localization.defaultLocale
            }, (payload) => applyData({ ...data, localization: payload.localization, company: payload.company }));
          }}>
            <Typography variant="h5">Localization</Typography>
            {feedback.localization ? <Alert severity={feedback.localization.type}>{feedback.localization.message}</Alert> : null}
            <TextField label="Default language" value={drafts.localization.defaultLanguage} onChange={(event) => updateDrafts((current) => ({ ...current, localization: { ...current.localization, defaultLanguage: event.target.value } }))} select fullWidth>
              {data.localization.supportedLanguages.map((language) => (
                <MenuItem key={language} value={language}>{language}</MenuItem>
              ))}
            </TextField>
            <TextField label="Default locale" value={drafts.localization.defaultLocale} onChange={(event) => updateDrafts((current) => ({ ...current, localization: { ...current.localization, defaultLocale: event.target.value } }))} select fullWidth>
              {data.localization.supportedLocales.map((locale) => (
                <MenuItem key={locale} value={locale}>{locale}</MenuItem>
              ))}
            </TextField>
            <Button type="submit" variant="contained" disabled={saving === "localization"}>{saving === "localization" ? "Saving localization..." : "Save localization"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, lg: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (!emailPattern.test(drafts.notifications.destinationEmail)) {
              setSectionFeedback("notifications", { type: "error", message: "Notification email must be valid." });
              return;
            }
            void saveSection<{
              message: string;
              notificationPreferences: CompanyBackofficeData["notificationPreferences"];
            }>("notifications", `/api/app/company/${slug}/notification-preferences`, "PUT", {
              destinationEmail: drafts.notifications.destinationEmail.trim(),
              notifyOnNewBooking: drafts.notifications.notifyOnNewBooking,
              notifyOnCancellation: drafts.notifications.notifyOnCancellation,
              notifyDailySummary: drafts.notifications.notifyDailySummary
            }, (payload) => applyData({ ...data, notificationPreferences: payload.notificationPreferences }));
          }}>
            <Typography variant="h5">Notification Preferences</Typography>
            {feedback.notifications ? <Alert severity={feedback.notifications.type}>{feedback.notifications.message}</Alert> : null}
            <TextField label="Destination email" value={drafts.notifications.destinationEmail} onChange={(event) => updateDrafts((current) => ({ ...current, notifications: { ...current.notifications, destinationEmail: event.target.value } }))} fullWidth />
            <FormControlLabel control={<Checkbox checked={drafts.notifications.notifyOnNewBooking} onChange={(event) => updateDrafts((current) => ({ ...current, notifications: { ...current.notifications, notifyOnNewBooking: event.target.checked } }))} />} label="Notify on new booking" />
            <FormControlLabel control={<Checkbox checked={drafts.notifications.notifyOnCancellation} onChange={(event) => updateDrafts((current) => ({ ...current, notifications: { ...current.notifications, notifyOnCancellation: event.target.checked } }))} />} label="Notify on cancellation" />
            <FormControlLabel control={<Checkbox checked={drafts.notifications.notifyDailySummary} onChange={(event) => updateDrafts((current) => ({ ...current, notifications: { ...current.notifications, notifyDailySummary: event.target.checked } }))} />} label="Send daily summary" />
            <Button type="submit" variant="contained" disabled={saving === "notifications"}>{saving === "notifications" ? "Saving notifications..." : "Save notification preferences"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, lg: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (!emailPattern.test(drafts.bookingTriggers.destinationEmail)) {
              setSectionFeedback("bookingTriggers", { type: "error", message: "Booking notification email must be valid." });
              return;
            }
            void saveSection<{
              message: string;
              bookingNotificationTriggers: CompanyBackofficeData["bookingNotificationTriggers"];
            }>("bookingTriggers", `/api/app/company/${slug}/booking-notification-triggers`, "PUT", {
              destinationEmail: drafts.bookingTriggers.destinationEmail.trim(),
              notifyOnNewBooking: drafts.bookingTriggers.notifyOnNewBooking,
              notifyOnBookingConfirmed: drafts.bookingTriggers.notifyOnBookingConfirmed,
              notifyOnCancellation: drafts.bookingTriggers.notifyOnCancellation,
              notifyOnBookingCompleted: drafts.bookingTriggers.notifyOnBookingCompleted,
              notifyOnBookingNoShow: drafts.bookingTriggers.notifyOnBookingNoShow
            }, (payload) => applyData({ ...data, bookingNotificationTriggers: payload.bookingNotificationTriggers }));
          }}>
            <Typography variant="h5">Booking Notification Triggers</Typography>
            {feedback.bookingTriggers ? <Alert severity={feedback.bookingTriggers.type}>{feedback.bookingTriggers.message}</Alert> : null}
            <TextField label="Booking destination email" value={drafts.bookingTriggers.destinationEmail} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, destinationEmail: event.target.value } }))} fullWidth />
            <FormControlLabel control={<Checkbox checked={drafts.bookingTriggers.notifyOnNewBooking} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, notifyOnNewBooking: event.target.checked } }))} />} label="Notify on new booking requests" />
            <FormControlLabel control={<Checkbox checked={drafts.bookingTriggers.notifyOnBookingConfirmed} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, notifyOnBookingConfirmed: event.target.checked } }))} />} label="Notify on booking confirmation" />
            <FormControlLabel control={<Checkbox checked={drafts.bookingTriggers.notifyOnCancellation} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, notifyOnCancellation: event.target.checked } }))} />} label="Notify on booking cancellation" />
            <FormControlLabel control={<Checkbox checked={drafts.bookingTriggers.notifyOnBookingCompleted} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, notifyOnBookingCompleted: event.target.checked } }))} />} label="Notify on booking completion" />
            <FormControlLabel control={<Checkbox checked={drafts.bookingTriggers.notifyOnBookingNoShow} onChange={(event) => updateDrafts((current) => ({ ...current, bookingTriggers: { ...current.bookingTriggers, notifyOnBookingNoShow: event.target.checked } }))} />} label="Notify on no-show" />
            <Button type="submit" variant="contained" disabled={saving === "bookingTriggers"}>{saving === "bookingTriggers" ? "Saving booking triggers..." : "Save booking triggers"}</Button>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
