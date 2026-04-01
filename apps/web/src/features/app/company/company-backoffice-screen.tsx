"use client";

import { useEffect, useRef, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Checkbox,
  FormControlLabel,
  Grid,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography
} from "@mui/material";
import { useRouter } from "next/navigation";
import { LogoutButton } from "@/components/app/logout-button";
import { CsrfTokenError, fetchCsrfToken } from "@/lib/security/csrf";

type CompanyBackofficeData = {
  company: {
    companyName: string;
    companySlug: string;
    businessType: string;
    companyStatus: string;
    defaultLanguage: string;
    defaultLocale: string;
    createdAt: string;
  };
  profile: {
    businessDescription: string | null;
    contactEmail: string | null;
    contactPhone: string | null;
    addressLine1: string | null;
    addressLine2: string | null;
    city: string | null;
    postalCode: string | null;
    countryCode: string | null;
  };
  branding: {
    displayName: string | null;
    logoUrl: string | null;
    accentColor: string | null;
    supportEmail: string | null;
    supportPhone: string | null;
  };
  localization: {
    defaultLanguage: string;
    defaultLocale: string;
    supportedLanguages: string[];
    supportedLocales: string[];
  };
  businessHours: Array<{
    id?: number | null;
    dayOfWeek: string;
    opensAt: string;
    closesAt: string;
    displayOrder: number;
  }>;
  closureDates: Array<{
    id?: number | null;
    label: string | null;
    startsOn: string;
    endsOn: string;
  }>;
  notificationPreferences: {
    destinationEmail: string | null;
    notifyOnNewBooking: boolean;
    notifyOnCancellation: boolean;
    notifyDailySummary: boolean;
  };
  bookingNotificationTriggers: {
    destinationEmail: string | null;
    notifyOnNewBooking: boolean;
    notifyOnBookingConfirmed: boolean;
    notifyOnCancellation: boolean;
    notifyOnBookingCompleted: boolean;
    notifyOnBookingNoShow: boolean;
  };
  customerContacts: Array<{
    id: number;
    fullName: string;
    email: string;
    phone: string | null;
    preferredLanguage: string | null;
    notes: string | null;
    createdAt: string;
    updatedAt: string;
  }>;
  bookings: Array<{
    id: number;
    customerContactId: number;
    customerName: string;
    customerEmail: string;
    status: string;
    source: string;
    requestSummary: string | null;
    preferredDate: string | null;
    internalNote: string | null;
    createdAt: string;
    updatedAt: string;
  }>;
  bookingAudit: Array<{
    id: number;
    bookingId: number;
    actionType: string;
    actorEmail: string | null;
    outcome: string;
    details: string | null;
    createdAt: string;
  }>;
  staffUsers: Array<{
    membershipId: number;
    userId?: number;
    fullName: string | null;
    email: string;
    role: string;
    status: string;
    emailVerified: boolean;
    createdAt?: string;
  }>;
  customerQuestions: Array<{
    id?: number | null;
    label: string;
    questionType: string;
    required: boolean;
    enabled: boolean;
    displayOrder: number;
    options: string[];
  }>;
  widgetSettings: {
    ctaLabel: string | null;
    widgetEnabled: boolean;
    allowedDomains: string[];
    themeVariant: string;
  };
  viewer: { role: string; currentUserEmail: string };
  operations: {
    planType: string;
    subscriptionExpiresAt: string | null;
    staffCount: number;
    adminCount: number;
    lastActivityAt: string;
    deletionScheduledAt: string | null;
  };
  configurationAreas: Array<{ key: string; title: string; description: string; status: string }>;
};

type Props = { slug: string };
type Feedback = Record<string, { type: "success" | "error"; message: string } | null>;
type State =
  | { status: "loading" }
  | { status: "loaded"; data: CompanyBackofficeData }
  | { status: "forbidden" }
  | { status: "error"; message: string };

type Drafts = {
  profile: {
    companyName: string;
    businessDescription: string;
    contactEmail: string;
    contactPhone: string;
    addressLine1: string;
    addressLine2: string;
    city: string;
    postalCode: string;
    countryCode: string;
  };
  branding: {
    displayName: string;
    logoUrl: string;
    accentColor: string;
    supportEmail: string;
    supportPhone: string;
  };
  localization: {
    defaultLanguage: string;
    defaultLocale: string;
  };
  businessHours: Array<{
    dayOfWeek: string;
    opensAt: string;
    closesAt: string;
    displayOrder: number;
  }>;
  closureDates: Array<{
    label: string;
    startsOn: string;
    endsOn: string;
  }>;
  notifications: {
    destinationEmail: string;
    notifyOnNewBooking: boolean;
    notifyOnCancellation: boolean;
    notifyDailySummary: boolean;
  };
  bookingTriggers: {
    destinationEmail: string;
    notifyOnNewBooking: boolean;
    notifyOnBookingConfirmed: boolean;
    notifyOnCancellation: boolean;
    notifyOnBookingCompleted: boolean;
    notifyOnBookingNoShow: boolean;
  };
  contactCreate: {
    fullName: string;
    email: string;
    phone: string;
    preferredLanguage: string;
    notes: string;
  };
  contactUpdate: Record<number, {
    fullName: string;
    email: string;
    phone: string;
    preferredLanguage: string;
    notes: string;
  }>;
  staffCreate: {
    fullName: string;
    email: string;
    role: string;
  };
  staffUpdate: Record<number, { role: string; status: string }>;
  bookingUpdate: Record<number, { status: string; internalNote: string }>;
  questions: Array<{
    label: string;
    questionType: string;
    required: boolean;
    enabled: boolean;
    displayOrder: number;
    optionsText: string;
  }>;
  widget: {
    ctaLabel: string;
    widgetEnabled: boolean;
    allowedDomainsText: string;
    themeVariant: string;
  };
};

type ApiMessageResponse = { message?: string } | null;

const days = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];
const roles = ["COMPANY_ADMIN", "STAFF"];
const statuses = ["ACTIVE", "INACTIVE"];
const bookingStatuses = ["PENDING", "CONFIRMED", "CANCELLED", "COMPLETED", "NO_SHOW"];
const questionTypes = ["SHORT_TEXT", "LONG_TEXT", "SINGLE_SELECT", "CHECKBOX"];
const widgetThemes = ["minimal", "soft", "contrast"];

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const phonePattern = /^[0-9+()\-\s]{7,}$/;
const colorPattern = /^#[0-9A-Fa-f]{6}$/;
const domainPattern = /^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+[a-z]{2,}$/;

function formatUtcDateTime(value: string | null) {
  return value ? new Date(value).toLocaleString("en-GB", { timeZone: "UTC" }) : "Not scheduled";
}

function textList(value: string[]) {
  return value.join("\n");
}

function splitTextList(value: string) {
  return value.split("\n").map((item) => item.trim()).filter(Boolean);
}

function buildDrafts(data: CompanyBackofficeData): Drafts {
  return {
    profile: {
      companyName: data.company.companyName,
      businessDescription: data.profile.businessDescription ?? "",
      contactEmail: data.profile.contactEmail ?? "",
      contactPhone: data.profile.contactPhone ?? "",
      addressLine1: data.profile.addressLine1 ?? "",
      addressLine2: data.profile.addressLine2 ?? "",
      city: data.profile.city ?? "",
      postalCode: data.profile.postalCode ?? "",
      countryCode: data.profile.countryCode ?? ""
    },
    branding: {
      displayName: data.branding.displayName ?? "",
      logoUrl: data.branding.logoUrl ?? "",
      accentColor: data.branding.accentColor ?? "#B45A38",
      supportEmail: data.branding.supportEmail ?? data.profile.contactEmail ?? "",
      supportPhone: data.branding.supportPhone ?? data.profile.contactPhone ?? ""
    },
    localization: {
      defaultLanguage: data.localization.defaultLanguage,
      defaultLocale: data.localization.defaultLocale
    },
    businessHours: data.businessHours.length > 0 ? data.businessHours.map((entry) => ({
      dayOfWeek: entry.dayOfWeek,
      opensAt: entry.opensAt,
      closesAt: entry.closesAt,
      displayOrder: entry.displayOrder
    })) : [{ dayOfWeek: "MONDAY", opensAt: "09:00", closesAt: "17:00", displayOrder: 0 }],
    closureDates: data.closureDates.map((entry) => ({
      label: entry.label ?? "",
      startsOn: entry.startsOn,
      endsOn: entry.endsOn
    })),
    notifications: {
      destinationEmail: data.notificationPreferences.destinationEmail ?? data.profile.contactEmail ?? "",
      notifyOnNewBooking: data.notificationPreferences.notifyOnNewBooking,
      notifyOnCancellation: data.notificationPreferences.notifyOnCancellation,
      notifyDailySummary: data.notificationPreferences.notifyDailySummary
    },
    bookingTriggers: {
      destinationEmail: data.bookingNotificationTriggers.destinationEmail ?? data.profile.contactEmail ?? "",
      notifyOnNewBooking: data.bookingNotificationTriggers.notifyOnNewBooking,
      notifyOnBookingConfirmed: data.bookingNotificationTriggers.notifyOnBookingConfirmed,
      notifyOnCancellation: data.bookingNotificationTriggers.notifyOnCancellation,
      notifyOnBookingCompleted: data.bookingNotificationTriggers.notifyOnBookingCompleted,
      notifyOnBookingNoShow: data.bookingNotificationTriggers.notifyOnBookingNoShow
    },
    contactCreate: {
      fullName: "",
      email: "",
      phone: "",
      preferredLanguage: data.company.defaultLanguage,
      notes: ""
    },
    contactUpdate: Object.fromEntries(data.customerContacts.map((contact) => [contact.id, {
      fullName: contact.fullName,
      email: contact.email,
      phone: contact.phone ?? "",
      preferredLanguage: contact.preferredLanguage ?? data.company.defaultLanguage,
      notes: contact.notes ?? ""
    }])),
    staffCreate: { fullName: "", email: "", role: "STAFF" },
    staffUpdate: Object.fromEntries(data.staffUsers.map((user) => [user.membershipId, { role: user.role, status: user.status }])),
    bookingUpdate: Object.fromEntries(data.bookings.map((booking) => [booking.id, { status: booking.status, internalNote: booking.internalNote ?? "" }])),
    questions: data.customerQuestions.map((question) => ({
      label: question.label,
      questionType: question.questionType,
      required: question.required,
      enabled: question.enabled,
      displayOrder: question.displayOrder,
      optionsText: textList(question.options)
    })),
    widget: {
      ctaLabel: data.widgetSettings.ctaLabel ?? "",
      widgetEnabled: data.widgetSettings.widgetEnabled,
      allowedDomainsText: textList(data.widgetSettings.allowedDomains),
      themeVariant: data.widgetSettings.themeVariant
    }
  };
}

export function CompanyBackofficeScreen({ slug }: Props) {
  const router = useRouter();
  const routerRef = useRef(router);
  const [state, setState] = useState<State>({ status: "loading" });
  const [drafts, setDrafts] = useState<Drafts | null>(null);
  const [feedback, setFeedback] = useState<Feedback>({});
  const [saving, setSaving] = useState<string | null>(null);

  function setSectionFeedback(section: string, next: Feedback[string]) {
    setFeedback((current) => ({ ...current, [section]: next }));
  }

  function hydrate(data: CompanyBackofficeData) {
    setDrafts(buildDrafts(data));
  }

  function updateDrafts(updater: (current: Drafts) => Drafts) {
    setDrafts((current) => (current ? updater(current) : current));
  }

  routerRef.current = router;

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/app/company/${slug}/backoffice`, {
          credentials: "include"
        });
        if (!active) return;
        if (response.status === 401) {
          routerRef.current.replace("/en/login");
          return;
        }
        if (response.status === 403) {
          setState({ status: "forbidden" });
          return;
        }
        if (!response.ok) {
          setState({ status: "error", message: "The company backoffice could not be loaded." });
          return;
        }
        const payload = (await response.json()) as CompanyBackofficeData;
        hydrate(payload);
        setState({ status: "loaded", data: payload });
      } catch {
        if (active) {
          setState({ status: "error", message: "The company backoffice could not be loaded." });
        }
      }
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
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}${endpoint}`, {
        method,
        credentials: "include",
        headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": csrfToken },
        body: JSON.stringify(body)
      });
      const payload = (await response.json().catch(() => null)) as TResponse;
      if (response.status === 401) {
        routerRef.current.replace("/en/login");
        return;
      }
      if (response.status === 403) {
        setState({ status: "forbidden" });
        return;
      }
      if (!response.ok || payload == null) {
        setSectionFeedback(section, { type: "error", message: payload?.message ?? "The changes could not be saved." });
        return;
      }
      apply(payload);
      setSectionFeedback(section, { type: "success", message: payload.message ?? "Changes saved." });
    } catch (error) {
      if (error instanceof CsrfTokenError && error.status === 401) {
        routerRef.current.replace("/en/login");
      } else {
        setSectionFeedback(section, { type: "error", message: "The changes could not be saved." });
      }
    } finally {
      setSaving(null);
    }
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

  const data = state.data;

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper sx={{ maxWidth: 1280, mx: "auto", p: { xs: 3, md: 5 } }}>
        <Stack spacing={4}>
          <Stack spacing={1}>
            <Typography variant="h3">{data.company.companyName}</Typography>
            <Typography color="text.secondary">
              Shared company configuration dashboard for secure tenant-scoped settings, staffing, and booking setup.
            </Typography>
          </Stack>
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
                </Stack>
              </Paper>
            </Grid>
          </Grid>

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
                  }, (payload) => {
                    const nextData = { ...data, company: payload.company, profile: payload.profile };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                  }, (payload) => {
                    const nextData = { ...data, branding: payload.branding };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                  }, (payload) => {
                    const nextData = { ...data, localization: payload.localization, company: payload.company };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                  }, (payload) => {
                    const nextData = { ...data, notificationPreferences: payload.notificationPreferences };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                  }, (payload) => {
                    const nextData = { ...data, bookingNotificationTriggers: payload.bookingNotificationTriggers };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                    }, (payload) => {
                      const nextData = { ...data, customerContacts: [...data.customerContacts, payload.customerContact] };
                      setState({ status: "loaded", data: nextData });
                      hydrate(nextData);
                    });
                  }}>
                    <TextField label="Contact full name" value={drafts.contactCreate.fullName} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, fullName: event.target.value } }))} fullWidth />
                    <TextField label="Contact email" value={drafts.contactCreate.email} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, email: event.target.value } }))} fullWidth />
                    <TextField label="Contact phone" value={drafts.contactCreate.phone} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, phone: event.target.value } }))} fullWidth />
                    <TextField label="Preferred language" value={drafts.contactCreate.preferredLanguage} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, preferredLanguage: event.target.value } }))} select fullWidth>
                      {data.localization.supportedLanguages.map((language) => <MenuItem key={language} value={language}>{language}</MenuItem>)}
                    </TextField>
                    <TextField label="Contact notes" value={drafts.contactCreate.notes} onChange={(event) => updateDrafts((current) => ({ ...current, contactCreate: { ...current.contactCreate, notes: event.target.value } }))} multiline minRows={3} fullWidth />
                    <Button type="submit" variant="contained" disabled={saving === "contacts"}>{saving === "contacts" ? "Saving contact..." : "Create customer contact"}</Button>
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
                          }, (payload) => {
                            const nextData = { ...data, customerContacts: data.customerContacts.map((item) => item.id === contact.id ? payload.customerContact : item) };
                            setState({ status: "loaded", data: nextData });
                            hydrate(nextData);
                          });
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
                          }, (payload) => {
                            const nextData = { ...data, bookings: data.bookings.map((item) => item.id === booking.id ? payload.booking : item) };
                            setState({ status: "loaded", data: nextData });
                            hydrate(nextData);
                          });
                        }}>Update booking status</Button>
                      </Stack>
                    </Paper>
                  ))}
                </Stack>
              </Paper>
            </Grid>
          </Grid>

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
                  }, (payload) => {
                    const nextData = { ...data, businessHours: payload.businessHours };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                  }, (payload) => {
                    const nextData = { ...data, closureDates: payload.closureDates };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
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
                              const nextData = {
                                ...data,
                                staffUsers: nextUsers,
                                operations: {
                                  ...data.operations,
                                  adminCount: nextUsers.filter((item) => item.role === "COMPANY_ADMIN").length
                                }
                              };
                              setState({ status: "loaded", data: nextData });
                              hydrate(nextData);
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
                    const nextData = {
                      ...data,
                      staffUsers: nextUsers,
                      operations: {
                        ...data.operations,
                        staffCount: nextUsers.length,
                        adminCount: nextUsers.filter((item) => item.role === "COMPANY_ADMIN").length
                      }
                    };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
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

          <Grid container spacing={3}>
            <Grid size={{ xs: 12, xl: 7 }}>
              <Paper variant="outlined" sx={{ p: 3 }}>
                <Stack spacing={2} component="form" onSubmit={(event) => {
                  event.preventDefault();
                  void saveSection<{
                    message: string;
                    customerQuestions: CompanyBackofficeData["customerQuestions"];
                  }>("questions", `/api/app/company/${slug}/customer-questions`, "PUT", {
                    entries: drafts.questions.map((question, index) => ({
                      label: question.label.trim(),
                      questionType: question.questionType,
                      required: question.required,
                      enabled: question.enabled,
                      displayOrder: index,
                      options: splitTextList(question.optionsText)
                    }))
                  }, (payload) => {
                    const nextData = { ...data, customerQuestions: payload.customerQuestions };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
                }}>
                  <Stack direction="row" justifyContent="space-between">
                    <Typography variant="h5">Customer Questions</Typography>
                    <Button onClick={() => updateDrafts((current) => ({
                      ...current,
                      questions: [...current.questions, { label: "", questionType: "SHORT_TEXT", required: false, enabled: true, displayOrder: current.questions.length, optionsText: "" }]
                    }))}>Add question</Button>
                  </Stack>
                  {feedback.questions ? <Alert severity={feedback.questions.type}>{feedback.questions.message}</Alert> : null}
                  {drafts.questions.map((question, index) => (
                    <Paper key={`${question.label}-${index}`} sx={{ p: 2 }} variant="outlined">
                      <Stack spacing={1}>
                        <TextField label="Question label" value={question.label} onChange={(event) => updateDrafts((current) => ({
                          ...current,
                          questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, label: event.target.value } : item)
                        }))} fullWidth />
                        <TextField label="Question type" value={question.questionType} onChange={(event) => updateDrafts((current) => ({
                          ...current,
                          questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, questionType: event.target.value } : item)
                        }))} select fullWidth>
                          {questionTypes.map((type) => <MenuItem key={type} value={type}>{type}</MenuItem>)}
                        </TextField>
                        <FormControlLabel control={<Checkbox checked={question.required} onChange={(event) => updateDrafts((current) => ({
                          ...current,
                          questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, required: event.target.checked } : item)
                        }))} />} label="Required" />
                        <FormControlLabel control={<Checkbox checked={question.enabled} onChange={(event) => updateDrafts((current) => ({
                          ...current,
                          questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, enabled: event.target.checked } : item)
                        }))} />} label="Enabled" />
                        {question.questionType === "SINGLE_SELECT" ? <TextField label="Selectable options" helperText="One option per line" value={question.optionsText} onChange={(event) => updateDrafts((current) => ({
                          ...current,
                          questions: current.questions.map((item, itemIndex) => itemIndex === index ? { ...item, optionsText: event.target.value } : item)
                        }))} multiline minRows={3} fullWidth /> : null}
                        <Stack direction="row" spacing={1}>
                          <Button disabled={index === 0} onClick={() => updateDrafts((current) => {
                            const next = [...current.questions];
                            [next[index - 1], next[index]] = [next[index], next[index - 1]];
                            return { ...current, questions: next };
                          })}>Move up</Button>
                          <Button disabled={index === drafts.questions.length - 1} onClick={() => updateDrafts((current) => {
                            const next = [...current.questions];
                            [next[index], next[index + 1]] = [next[index + 1], next[index]];
                            return { ...current, questions: next };
                          })}>Move down</Button>
                          <Button color="error" onClick={() => updateDrafts((current) => ({
                            ...current,
                            questions: current.questions.filter((_, itemIndex) => itemIndex !== index)
                          }))}>Remove</Button>
                        </Stack>
                      </Stack>
                    </Paper>
                  ))}
                  <Button type="submit" variant="contained" disabled={saving === "questions"}>{saving === "questions" ? "Saving questions..." : "Save customer questions"}</Button>
                </Stack>
              </Paper>
            </Grid>

            <Grid size={{ xs: 12, xl: 5 }}>
              <Paper variant="outlined" sx={{ p: 3 }}>
                <Stack spacing={2} component="form" onSubmit={(event) => {
                  event.preventDefault();
                  if (splitTextList(drafts.widget.allowedDomainsText).some((domain) => !domainPattern.test(domain.toLowerCase()))) {
                    setSectionFeedback("widget", { type: "error", message: "Allowed widget domains must be valid host names." });
                    return;
                  }
                  void saveSection<{
                    message: string;
                    widgetSettings: CompanyBackofficeData["widgetSettings"];
                  }>("widget", `/api/app/company/${slug}/widget-settings`, "PUT", {
                    ctaLabel: drafts.widget.ctaLabel.trim() || null,
                    widgetEnabled: drafts.widget.widgetEnabled,
                    allowedDomains: splitTextList(drafts.widget.allowedDomainsText),
                    themeVariant: drafts.widget.themeVariant
                  }, (payload) => {
                    const nextData = { ...data, widgetSettings: payload.widgetSettings };
                    setState({ status: "loaded", data: nextData });
                    hydrate(nextData);
                  });
                }}>
                  <Typography variant="h5">Widget Settings</Typography>
                  {feedback.widget ? <Alert severity={feedback.widget.type}>{feedback.widget.message}</Alert> : null}
                  <TextField label="CTA label" value={drafts.widget.ctaLabel} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, ctaLabel: event.target.value } }))} fullWidth />
                  <FormControlLabel control={<Checkbox checked={drafts.widget.widgetEnabled} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, widgetEnabled: event.target.checked } }))} />} label="Widget enabled" />
                  <TextField label="Theme variant" value={drafts.widget.themeVariant} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, themeVariant: event.target.value } }))} select fullWidth>
                    {widgetThemes.map((theme) => <MenuItem key={theme} value={theme}>{theme}</MenuItem>)}
                  </TextField>
                  <TextField label="Allowed domains" helperText="One host name per line" value={drafts.widget.allowedDomainsText} onChange={(event) => updateDrafts((current) => ({ ...current, widget: { ...current.widget, allowedDomainsText: event.target.value } }))} multiline minRows={4} fullWidth />
                  <Button type="submit" variant="contained" disabled={saving === "widget"}>{saving === "widget" ? "Saving widget settings..." : "Save widget settings"}</Button>
                </Stack>
              </Paper>
            </Grid>
          </Grid>

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

          <LogoutButton />
        </Stack>
      </Paper>
    </Box>
  );
}
