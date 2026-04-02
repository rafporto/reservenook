"use client";

import { Alert, Button, Grid, Paper, Stack, TextField, Typography } from "@mui/material";
import { colorPattern, emailPattern, phonePattern, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function ProfileAndBrandingPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, lg: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (!drafts.profile.companyName.trim() || !emailPattern.test(drafts.profile.contactEmail) || !phonePattern.test(drafts.profile.contactPhone)) {
              setSectionFeedback("profile", { type: "error", message: "Company name, contact email, and contact phone must be valid." });
              return;
            }
            void saveSection<{
              message: string;
              company: CompanyBackofficeData["company"];
              profile: CompanyBackofficeData["profile"];
            }>("profile", `/api/app/company/${slug}/profile`, "PUT", {
              companyName: drafts.profile.companyName.trim(),
              businessDescription: drafts.profile.businessDescription.trim() || null,
              contactEmail: drafts.profile.contactEmail.trim(),
              contactPhone: drafts.profile.contactPhone.trim(),
              addressLine1: drafts.profile.addressLine1.trim(),
              addressLine2: drafts.profile.addressLine2.trim() || null,
              city: drafts.profile.city.trim(),
              postalCode: drafts.profile.postalCode.trim(),
              countryCode: drafts.profile.countryCode.trim().toUpperCase()
            }, (payload) => applyData({ ...data, company: payload.company, profile: payload.profile }));
          }}>
            <Typography variant="h5">Company Profile</Typography>
            {feedback.profile ? <Alert severity={feedback.profile.type}>{feedback.profile.message}</Alert> : null}
            <TextField label="Company name" value={drafts.profile.companyName} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, companyName: event.target.value } }))} fullWidth />
            <TextField label="Business description" value={drafts.profile.businessDescription} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, businessDescription: event.target.value } }))} multiline minRows={3} fullWidth />
            <TextField label="Primary contact email" value={drafts.profile.contactEmail} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, contactEmail: event.target.value } }))} fullWidth />
            <TextField label="Primary contact phone" value={drafts.profile.contactPhone} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, contactPhone: event.target.value } }))} fullWidth />
            <TextField label="Address line 1" value={drafts.profile.addressLine1} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, addressLine1: event.target.value } }))} fullWidth />
            <TextField label="Address line 2" value={drafts.profile.addressLine2} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, addressLine2: event.target.value } }))} fullWidth />
            <Grid container spacing={2}>
              <Grid size={{ xs: 12, md: 4 }}>
                <TextField label="City" value={drafts.profile.city} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, city: event.target.value } }))} fullWidth />
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <TextField label="Postal code" value={drafts.profile.postalCode} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, postalCode: event.target.value } }))} fullWidth />
              </Grid>
              <Grid size={{ xs: 12, md: 4 }}>
                <TextField label="Country code" value={drafts.profile.countryCode} onChange={(event) => updateDrafts((current) => ({ ...current, profile: { ...current.profile, countryCode: event.target.value } }))} fullWidth />
              </Grid>
            </Grid>
            <Button type="submit" variant="contained" disabled={saving === "profile"}>{saving === "profile" ? "Saving profile..." : "Save company profile"}</Button>
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, lg: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (!colorPattern.test(drafts.branding.accentColor) || !emailPattern.test(drafts.branding.supportEmail) || !phonePattern.test(drafts.branding.supportPhone)) {
              setSectionFeedback("branding", { type: "error", message: "Branding values are invalid." });
              return;
            }
            void saveSection<{
              message: string;
              branding: CompanyBackofficeData["branding"];
            }>("branding", `/api/app/company/${slug}/branding`, "PUT", {
              displayName: drafts.branding.displayName.trim() || null,
              logoUrl: drafts.branding.logoUrl.trim() || null,
              accentColor: drafts.branding.accentColor.trim(),
              supportEmail: drafts.branding.supportEmail.trim(),
              supportPhone: drafts.branding.supportPhone.trim()
            }, (payload) => applyData({ ...data, branding: payload.branding }));
          }}>
            <Typography variant="h5">Branding</Typography>
            {feedback.branding ? <Alert severity={feedback.branding.type}>{feedback.branding.message}</Alert> : null}
            <TextField label="Brand display name" value={drafts.branding.displayName} onChange={(event) => updateDrafts((current) => ({ ...current, branding: { ...current.branding, displayName: event.target.value } }))} fullWidth />
            <TextField label="Logo URL" value={drafts.branding.logoUrl} onChange={(event) => updateDrafts((current) => ({ ...current, branding: { ...current.branding, logoUrl: event.target.value } }))} fullWidth />
            <TextField label="Accent color" value={drafts.branding.accentColor} onChange={(event) => updateDrafts((current) => ({ ...current, branding: { ...current.branding, accentColor: event.target.value } }))} fullWidth />
            <TextField label="Support email" value={drafts.branding.supportEmail} onChange={(event) => updateDrafts((current) => ({ ...current, branding: { ...current.branding, supportEmail: event.target.value } }))} fullWidth />
            <TextField label="Support phone" value={drafts.branding.supportPhone} onChange={(event) => updateDrafts((current) => ({ ...current, branding: { ...current.branding, supportPhone: event.target.value } }))} fullWidth />
            <Button type="submit" variant="contained" disabled={saving === "branding"}>{saving === "branding" ? "Saving branding..." : "Save branding"}</Button>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
