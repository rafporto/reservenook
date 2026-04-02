"use client";

import { Alert, Button, Checkbox, FormControlLabel, Grid, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import {
  classBookingStatuses,
  classSessionStatuses,
  CompanyBackofficeData,
  emailPattern,
  formatUtcDateTime
} from "@/features/app/company/company-backoffice-types";
import { SectionProps } from "@/features/app/company/company-backoffice-panel-types";

export function ClassManagementPanels(props: SectionProps) {
  const { slug, data, drafts, feedback, saving, updateDrafts, saveSection, setSectionFeedback, applyData } = props;
  const classTypes = data.classTypes ?? [];
  const classInstructors = data.classInstructors ?? [];
  const classSessions = data.classSessions ?? [];
  const classBookings = data.classBookings ?? [];

  return (
    <Grid container spacing={3}>
      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Class Types</Typography>
            {feedback.classTypes ? <Alert severity={feedback.classTypes.type}>{feedback.classTypes.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const durationMinutes = Number(drafts.classTypeCreate.durationMinutes);
              const defaultCapacity = Number(drafts.classTypeCreate.defaultCapacity);
              if (!drafts.classTypeCreate.name.trim() || !Number.isInteger(durationMinutes) || !Number.isInteger(defaultCapacity)) {
                setSectionFeedback("classTypes", { type: "error", message: "Class types require a name, duration, and capacity." });
                return;
              }
              void saveSection<{
                message: string;
                classType: CompanyBackofficeData["classTypes"][number];
              }>("classTypes", `/api/app/company/${slug}/class-types`, "POST", {
                name: drafts.classTypeCreate.name.trim(),
                description: drafts.classTypeCreate.description.trim() || null,
                durationMinutes,
                defaultCapacity,
                active: drafts.classTypeCreate.active,
                autoConfirm: drafts.classTypeCreate.autoConfirm
              }, (payload) => applyData({ ...data, classTypes: [...classTypes, payload.classType] }));
            }}>
              <TextField label="Class type name" value={drafts.classTypeCreate.name} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, name: event.target.value } }))} fullWidth />
              <TextField label="Description" value={drafts.classTypeCreate.description} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, description: event.target.value } }))} multiline minRows={2} fullWidth />
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Duration (min)" value={drafts.classTypeCreate.durationMinutes} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, durationMinutes: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Default capacity" value={drafts.classTypeCreate.defaultCapacity} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, defaultCapacity: event.target.value } }))} fullWidth />
                </Grid>
              </Grid>
              <FormControlLabel control={<Checkbox checked={drafts.classTypeCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, active: event.target.checked } }))} />} label="Class type active" />
              <FormControlLabel control={<Checkbox checked={drafts.classTypeCreate.autoConfirm} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeCreate: { ...current.classTypeCreate, autoConfirm: event.target.checked } }))} />} label="Auto confirm class bookings" />
              <Button type="submit" variant="contained" disabled={saving === "classTypes"}>{saving === "classTypes" ? "Saving class type..." : "Create class type"}</Button>
            </Stack>
            {classTypes.map((classType) => (
              <Paper key={classType.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{classType.name}</Typography>
                  <TextField label={`Class type name ${classType.id}`} value={drafts.classTypeUpdate[classType.id]?.name ?? classType.name} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], name: event.target.value } } }))} fullWidth />
                  <TextField label={`Description ${classType.id}`} value={drafts.classTypeUpdate[classType.id]?.description ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], description: event.target.value } } }))} multiline minRows={2} fullWidth />
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Duration ${classType.id}`} value={drafts.classTypeUpdate[classType.id]?.durationMinutes ?? String(classType.durationMinutes)} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], durationMinutes: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Capacity ${classType.id}`} value={drafts.classTypeUpdate[classType.id]?.defaultCapacity ?? String(classType.defaultCapacity)} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], defaultCapacity: event.target.value } } }))} fullWidth />
                    </Grid>
                  </Grid>
                  <FormControlLabel control={<Checkbox checked={drafts.classTypeUpdate[classType.id]?.active ?? classType.active} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], active: event.target.checked } } }))} />} label="Class type active" />
                  <FormControlLabel control={<Checkbox checked={drafts.classTypeUpdate[classType.id]?.autoConfirm ?? classType.autoConfirm} onChange={(event) => updateDrafts((current) => ({ ...current, classTypeUpdate: { ...current.classTypeUpdate, [classType.id]: { ...current.classTypeUpdate[classType.id], autoConfirm: event.target.checked } } }))} />} label="Auto confirm class bookings" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.classTypeUpdate[classType.id];
                    const durationMinutes = Number(draft?.durationMinutes ?? classType.durationMinutes);
                    const defaultCapacity = Number(draft?.defaultCapacity ?? classType.defaultCapacity);
                    if (!draft?.name.trim() || !Number.isInteger(durationMinutes) || !Number.isInteger(defaultCapacity)) {
                      setSectionFeedback("classTypes", { type: "error", message: "Class types require a name, duration, and capacity." });
                      return;
                    }
                    void saveSection<{
                      message: string;
                      classType: CompanyBackofficeData["classTypes"][number];
                    }>("classTypes", `/api/app/company/${slug}/class-types/${classType.id}`, "PUT", {
                      name: draft.name.trim(),
                      description: draft.description.trim() || null,
                      durationMinutes,
                      defaultCapacity,
                      active: draft.active,
                      autoConfirm: draft.autoConfirm
                    }, (payload) => applyData({ ...data, classTypes: data.classTypes.map((item) => item.id === classType.id ? payload.classType : item) }));
                  }}>Save class type</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Class Instructors</Typography>
            {feedback.classInstructors ? <Alert severity={feedback.classInstructors.type}>{feedback.classInstructors.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              if (!drafts.classInstructorCreate.displayName.trim()) {
                setSectionFeedback("classInstructors", { type: "error", message: "Instructors require a display name." });
                return;
              }
              if (drafts.classInstructorCreate.email.trim() && !emailPattern.test(drafts.classInstructorCreate.email.trim())) {
                setSectionFeedback("classInstructors", { type: "error", message: "Instructor email must be valid." });
                return;
              }
              void saveSection<{
                message: string;
                classInstructor: CompanyBackofficeData["classInstructors"][number];
              }>("classInstructors", `/api/app/company/${slug}/class-instructors`, "POST", {
                linkedUserId: drafts.classInstructorCreate.linkedUserId ? Number(drafts.classInstructorCreate.linkedUserId) : null,
                displayName: drafts.classInstructorCreate.displayName.trim(),
                email: drafts.classInstructorCreate.email.trim() || null,
                active: drafts.classInstructorCreate.active
              }, (payload) => applyData({ ...data, classInstructors: [...classInstructors, payload.classInstructor] }));
            }}>
              <TextField label="Instructor display name" value={drafts.classInstructorCreate.displayName} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorCreate: { ...current.classInstructorCreate, displayName: event.target.value } }))} fullWidth />
              <TextField label="Linked staff user" value={drafts.classInstructorCreate.linkedUserId} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorCreate: { ...current.classInstructorCreate, linkedUserId: event.target.value } }))} select fullWidth>
                <MenuItem value="">No linked user</MenuItem>
                {data.staffUsers.map((user) => <MenuItem key={user.userId ?? user.membershipId} value={String(user.userId ?? "")}>{user.fullName ?? user.email} ({user.email})</MenuItem>)}
              </TextField>
              <TextField label="Instructor email" value={drafts.classInstructorCreate.email} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorCreate: { ...current.classInstructorCreate, email: event.target.value } }))} fullWidth />
              <FormControlLabel control={<Checkbox checked={drafts.classInstructorCreate.active} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorCreate: { ...current.classInstructorCreate, active: event.target.checked } }))} />} label="Instructor active" />
              <Button type="submit" variant="contained" disabled={saving === "classInstructors"}>{saving === "classInstructors" ? "Saving instructor..." : "Create instructor"}</Button>
            </Stack>
            {classInstructors.map((instructor) => (
              <Paper key={instructor.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{instructor.displayName}</Typography>
                  <TextField label={`Instructor name ${instructor.id}`} value={drafts.classInstructorUpdate[instructor.id]?.displayName ?? instructor.displayName} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorUpdate: { ...current.classInstructorUpdate, [instructor.id]: { ...current.classInstructorUpdate[instructor.id], displayName: event.target.value } } }))} fullWidth />
                  <TextField label={`Linked user ${instructor.id}`} value={drafts.classInstructorUpdate[instructor.id]?.linkedUserId ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorUpdate: { ...current.classInstructorUpdate, [instructor.id]: { ...current.classInstructorUpdate[instructor.id], linkedUserId: event.target.value } } }))} select fullWidth>
                    <MenuItem value="">No linked user</MenuItem>
                    {data.staffUsers.map((user) => <MenuItem key={user.userId ?? user.membershipId} value={String(user.userId ?? "")}>{user.fullName ?? user.email} ({user.email})</MenuItem>)}
                  </TextField>
                  <TextField label={`Instructor email ${instructor.id}`} value={drafts.classInstructorUpdate[instructor.id]?.email ?? ""} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorUpdate: { ...current.classInstructorUpdate, [instructor.id]: { ...current.classInstructorUpdate[instructor.id], email: event.target.value } } }))} fullWidth />
                  <FormControlLabel control={<Checkbox checked={drafts.classInstructorUpdate[instructor.id]?.active ?? instructor.active} onChange={(event) => updateDrafts((current) => ({ ...current, classInstructorUpdate: { ...current.classInstructorUpdate, [instructor.id]: { ...current.classInstructorUpdate[instructor.id], active: event.target.checked } } }))} />} label="Instructor active" />
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.classInstructorUpdate[instructor.id];
                    if (!draft?.displayName.trim()) {
                      setSectionFeedback("classInstructors", { type: "error", message: "Instructors require a display name." });
                      return;
                    }
                    if (draft.email.trim() && !emailPattern.test(draft.email.trim())) {
                      setSectionFeedback("classInstructors", { type: "error", message: "Instructor email must be valid." });
                      return;
                    }
                    void saveSection<{
                      message: string;
                      classInstructor: CompanyBackofficeData["classInstructors"][number];
                    }>("classInstructors", `/api/app/company/${slug}/class-instructors/${instructor.id}`, "PUT", {
                      linkedUserId: draft.linkedUserId ? Number(draft.linkedUserId) : null,
                      displayName: draft.displayName.trim(),
                      email: draft.email.trim() || null,
                      active: draft.active
                    }, (payload) => applyData({ ...data, classInstructors: data.classInstructors.map((item) => item.id === instructor.id ? payload.classInstructor : item) }));
                  }}>Save instructor</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Class Sessions</Typography>
            {feedback.classSessions ? <Alert severity={feedback.classSessions.type}>{feedback.classSessions.message}</Alert> : null}
            <Stack spacing={2} component="form" onSubmit={(event) => {
              event.preventDefault();
              const capacity = Number(drafts.classSessionCreate.capacity);
              if (!drafts.classSessionCreate.classTypeId || !drafts.classSessionCreate.instructorId || !drafts.classSessionCreate.startsAt || !drafts.classSessionCreate.endsAt || !Number.isInteger(capacity)) {
                setSectionFeedback("classSessions", { type: "error", message: "Class sessions require a class type, instructor, times, and capacity." });
                return;
              }
              void saveSection<{
                message: string;
                classSession: CompanyBackofficeData["classSessions"][number];
              }>("classSessions", `/api/app/company/${slug}/class-sessions`, "POST", {
                classTypeId: Number(drafts.classSessionCreate.classTypeId),
                instructorId: Number(drafts.classSessionCreate.instructorId),
                startsAt: new Date(drafts.classSessionCreate.startsAt).toISOString(),
                endsAt: new Date(drafts.classSessionCreate.endsAt).toISOString(),
                capacity,
                status: drafts.classSessionCreate.status
              }, (payload) => applyData({ ...data, classSessions: [...classSessions, payload.classSession] }));
            }}>
              <TextField label="Class type" value={drafts.classSessionCreate.classTypeId} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, classTypeId: event.target.value } }))} select fullWidth>
                {classTypes.map((classType) => <MenuItem key={classType.id} value={String(classType.id)}>{classType.name}</MenuItem>)}
              </TextField>
              <TextField label="Instructor" value={drafts.classSessionCreate.instructorId} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, instructorId: event.target.value } }))} select fullWidth>
                {classInstructors.map((instructor) => <MenuItem key={instructor.id} value={String(instructor.id)}>{instructor.displayName}</MenuItem>)}
              </TextField>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Starts at" type="datetime-local" value={drafts.classSessionCreate.startsAt} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, startsAt: event.target.value } }))} InputLabelProps={{ shrink: true }} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Ends at" type="datetime-local" value={drafts.classSessionCreate.endsAt} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, endsAt: event.target.value } }))} InputLabelProps={{ shrink: true }} fullWidth />
                </Grid>
              </Grid>
              <Grid container spacing={2}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Capacity" value={drafts.classSessionCreate.capacity} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, capacity: event.target.value } }))} fullWidth />
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField label="Status" value={drafts.classSessionCreate.status} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionCreate: { ...current.classSessionCreate, status: event.target.value } }))} select fullWidth>
                    {classSessionStatuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                </Grid>
              </Grid>
              <Button type="submit" variant="contained" disabled={saving === "classSessions"}>{saving === "classSessions" ? "Saving class session..." : "Create class session"}</Button>
            </Stack>
            {classSessions.map((session) => (
              <Paper key={session.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{session.classTypeName}</Typography>
                  <Typography color="text.secondary">{session.instructorName} · {formatUtcDateTime(session.startsAt)}</Typography>
                  <Typography color="text.secondary">Confirmed: {session.confirmedCount} · Waitlist: {session.waitlistCount}</Typography>
                  <TextField label={`Class type ${session.id}`} value={drafts.classSessionUpdate[session.id]?.classTypeId ?? String(session.classTypeId)} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], classTypeId: event.target.value } } }))} select fullWidth>
                    {classTypes.map((classType) => <MenuItem key={classType.id} value={String(classType.id)}>{classType.name}</MenuItem>)}
                  </TextField>
                  <TextField label={`Instructor ${session.id}`} value={drafts.classSessionUpdate[session.id]?.instructorId ?? String(session.instructorId)} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], instructorId: event.target.value } } }))} select fullWidth>
                    {classInstructors.map((instructor) => <MenuItem key={instructor.id} value={String(instructor.id)}>{instructor.displayName}</MenuItem>)}
                  </TextField>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Starts at ${session.id}`} type="datetime-local" value={drafts.classSessionUpdate[session.id]?.startsAt ?? session.startsAt.slice(0, 16)} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], startsAt: event.target.value } } }))} InputLabelProps={{ shrink: true }} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Ends at ${session.id}`} type="datetime-local" value={drafts.classSessionUpdate[session.id]?.endsAt ?? session.endsAt.slice(0, 16)} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], endsAt: event.target.value } } }))} InputLabelProps={{ shrink: true }} fullWidth />
                    </Grid>
                  </Grid>
                  <Grid container spacing={2}>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Capacity ${session.id}`} value={drafts.classSessionUpdate[session.id]?.capacity ?? String(session.capacity)} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], capacity: event.target.value } } }))} fullWidth />
                    </Grid>
                    <Grid size={{ xs: 12, md: 6 }}>
                      <TextField label={`Status ${session.id}`} value={drafts.classSessionUpdate[session.id]?.status ?? session.status} onChange={(event) => updateDrafts((current) => ({ ...current, classSessionUpdate: { ...current.classSessionUpdate, [session.id]: { ...current.classSessionUpdate[session.id], status: event.target.value } } }))} select fullWidth>
                        {classSessionStatuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                      </TextField>
                    </Grid>
                  </Grid>
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.classSessionUpdate[session.id];
                    const capacity = Number(draft?.capacity ?? session.capacity);
                    if (!draft?.classTypeId || !draft?.instructorId || !draft.startsAt || !draft.endsAt || !Number.isInteger(capacity)) {
                      setSectionFeedback("classSessions", { type: "error", message: "Class sessions require a class type, instructor, times, and capacity." });
                      return;
                    }
                    void saveSection<{
                      message: string;
                      classSession: CompanyBackofficeData["classSessions"][number];
                    }>("classSessions", `/api/app/company/${slug}/class-sessions/${session.id}`, "PUT", {
                      classTypeId: Number(draft.classTypeId),
                      instructorId: Number(draft.instructorId),
                      startsAt: new Date(draft.startsAt).toISOString(),
                      endsAt: new Date(draft.endsAt).toISOString(),
                      capacity,
                      status: draft.status
                    }, (payload) => applyData({ ...data, classSessions: data.classSessions.map((item) => item.id === session.id ? payload.classSession : item) }));
                  }}>Save class session</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>

      <Grid size={{ xs: 12, xl: 6 }}>
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Stack spacing={2}>
            <Typography variant="h5">Class Bookings</Typography>
            {feedback.classBookings ? <Alert severity={feedback.classBookings.type}>{feedback.classBookings.message}</Alert> : null}
            {classBookings.map((booking) => (
              <Paper key={booking.id} variant="outlined" sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Typography variant="subtitle1">{booking.classTypeName}</Typography>
                  <Typography color="text.secondary">{booking.customerName} · {booking.customerEmail}</Typography>
                  <Typography color="text.secondary">{booking.instructorName} · {formatUtcDateTime(booking.startsAt)}</Typography>
                  {booking.waitlistPosition != null ? <Typography color="text.secondary">Waitlist position: {booking.waitlistPosition}</Typography> : null}
                  <TextField label={`Class booking status ${booking.id}`} value={drafts.classBookingUpdate[booking.id]?.status ?? booking.status} onChange={(event) => updateDrafts((current) => ({ ...current, classBookingUpdate: { ...current.classBookingUpdate, [booking.id]: { status: event.target.value } } }))} select fullWidth>
                    {classBookingStatuses.map((status) => <MenuItem key={status} value={status}>{status}</MenuItem>)}
                  </TextField>
                  <Button variant="outlined" onClick={() => {
                    const draft = drafts.classBookingUpdate[booking.id];
                    void saveSection<{
                      message: string;
                      classBooking: CompanyBackofficeData["classBookings"][number];
                    }>("classBookings", `/api/app/company/${slug}/class-bookings/${booking.id}/status`, "PUT", {
                      status: draft.status
                    }, (payload) => applyData({ ...data, classBookings: data.classBookings.map((item) => item.id === booking.id ? payload.classBooking : item) }));
                  }}>Update class booking</Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        </Paper>
      </Grid>
    </Grid>
  );
}
