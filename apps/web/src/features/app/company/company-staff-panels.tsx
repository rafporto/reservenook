"use client";

import { Alert, Button, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { emailPattern, roles, statuses, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function StaffPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Staff Users</Typography>
            {feedback.staff ? <Alert severity={feedback.staff.type}>{feedback.staff.message}</Alert> : null}
            {data.staffUsers.map((user) => (
              <Paper key={user.membershipId} sx={{ p: 2 }} variant="outlined">
                <Stack spacing={1}>
                  <Typography variant="h6">{user.fullName ?? user.email}</Typography>
                  <Typography color="text.secondary">{user.email}</Typography>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 5 }}>
                      <TextField label="Role" value={drafts.staffUpdate[user.membershipId]?.role ?? user.role} onChange={(event) => updateDrafts((current) => ({
                        ...current,
                        staffUpdate: {
                          ...current.staffUpdate,
                          [user.membershipId]: {
                            role: event.target.value,
                            status: current.staffUpdate[user.membershipId]?.status ?? user.status
                          }
                        }
                      }))} select fullWidth>
                        {roles.map((role) => <MenuItem key={role} value={role}>{role}</MenuItem>)}
                      </TextField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 5 }}>
                      <TextField label="Status" value={drafts.staffUpdate[user.membershipId]?.status ?? user.status} onChange={(event) => updateDrafts((current) => ({
                        ...current,
                        staffUpdate: {
                          ...current.staffUpdate,
                          [user.membershipId]: {
                            role: current.staffUpdate[user.membershipId]?.role ?? user.role,
                            status: event.target.value
                          }
                        }
                      }))} select fullWidth>
                        {statuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                      </TextField>
                    </Grid>
                    <Grid size={{ xs: 12, md: 2 }}>
                      <Button onClick={() => void saveSection<{
                        message: string;
                        staffUser: CompanyBackofficeData["staffUsers"][number];
                      }>("staff", `/api/app/company/${slug}/staff/${user.membershipId}`, "PUT", drafts.staffUpdate[user.membershipId], (payload) => {
                        const nextUsers = data.staffUsers.map((item) => item.membershipId === user.membershipId ? payload.staffUser : item);
                        applyData({
                          ...data,
                          staffUsers: nextUsers,
                          operations: {
                            ...data.operations,
                            adminCount: nextUsers.filter((item) => item.role === "COMPANY_ADMIN").length
                          }
                        });
                      })}>Save</Button>
                    </Grid>
                  </Grid>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2} component="form" onSubmit={(event) => {
            event.preventDefault();
            if (!drafts.staffCreate.fullName.trim() || !emailPattern.test(drafts.staffCreate.email.trim())) {
              setSectionFeedback("staff", { type: "error", message: "Staff full name and email must be valid." });
              return;
            }
            void saveSection<{
              message: string;
              staffUser: CompanyBackofficeData["staffUsers"][number];
            }>("staff", `/api/app/company/${slug}/staff`, "POST", {
              fullName: drafts.staffCreate.fullName.trim(),
              email: drafts.staffCreate.email.trim(),
              role: drafts.staffCreate.role
            }, (payload) => {
              const nextUsers = [...data.staffUsers, payload.staffUser];
              applyData({
                ...data,
                staffUsers: nextUsers,
                operations: {
                  ...data.operations,
                  staffCount: nextUsers.length,
                  adminCount: nextUsers.filter((item) => item.role === "COMPANY_ADMIN").length
                }
              });
              updateDrafts((current) => ({ ...current, staffCreate: { fullName: "", email: "", role: "STAFF" } }));
            });
          }}>
            <Typography variant="h5">Create Staff User</Typography>
            <TextField label="Full name" value={drafts.staffCreate.fullName} onChange={(event) => updateDrafts((current) => ({ ...current, staffCreate: { ...current.staffCreate, fullName: event.target.value } }))} fullWidth />
            <TextField label="Email" value={drafts.staffCreate.email} onChange={(event) => updateDrafts((current) => ({ ...current, staffCreate: { ...current.staffCreate, email: event.target.value } }))} fullWidth />
            <TextField label="Role" value={drafts.staffCreate.role} onChange={(event) => updateDrafts((current) => ({ ...current, staffCreate: { ...current.staffCreate, role: event.target.value } }))} select fullWidth>
              {roles.map((role) => <MenuItem key={role} value={role}>{role}</MenuItem>)}
            </TextField>
            <Typography color="text.secondary">New staff users receive a secure set-password invitation in the company language.</Typography>
            <Button type="submit" variant="contained" disabled={saving === "staff"}>{saving === "staff" ? "Creating staff user..." : "Create staff user"}</Button>
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
