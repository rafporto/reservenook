"use client";

import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Grid,
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
  viewer: {
    role: string;
    currentUserEmail: string;
  };
  operations: {
    planType: string;
    subscriptionExpiresAt: string | null;
    staffCount: number;
    adminCount: number;
    lastActivityAt: string;
    deletionScheduledAt: string | null;
  };
  configurationAreas: Array<{
    key: string;
    title: string;
    description: string;
    status: string;
  }>;
};

type ProfileDraft = {
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

type CompanyBackofficeScreenProps = {
  slug: string;
};

function formatUtcDateTime(value: string | null) {
  if (!value) {
    return "Not scheduled";
  }

  return new Date(value).toLocaleString("en-GB", { timeZone: "UTC" });
}

function toProfileDraft(data: CompanyBackofficeData): ProfileDraft {
  return {
    companyName: data.company.companyName,
    businessDescription: data.profile.businessDescription ?? "",
    contactEmail: data.profile.contactEmail ?? "",
    contactPhone: data.profile.contactPhone ?? "",
    addressLine1: data.profile.addressLine1 ?? "",
    addressLine2: data.profile.addressLine2 ?? "",
    city: data.profile.city ?? "",
    postalCode: data.profile.postalCode ?? "",
    countryCode: data.profile.countryCode ?? ""
  };
}

function validateProfileDraft(draft: ProfileDraft) {
  if (!draft.companyName.trim()) {
    return "Company name is required.";
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(draft.contactEmail.trim())) {
    return "Contact email must be a valid email address.";
  }

  if (!/^[0-9+()\-\s]{7,}$/.test(draft.contactPhone.trim())) {
    return "Contact phone must be a valid phone number.";
  }

  if (!draft.addressLine1.trim()) {
    return "Address line 1 is required.";
  }

  if (!draft.city.trim()) {
    return "City is required.";
  }

  if (!draft.postalCode.trim()) {
    return "Postal code is required.";
  }

  if (!/^[A-Za-z]{2}$/.test(draft.countryCode.trim())) {
    return "Country code must use the ISO 2-letter format.";
  }

  return null;
}

export function CompanyBackofficeScreen({ slug }: CompanyBackofficeScreenProps) {
  const router = useRouter();
  const [state, setState] = useState<
    | { status: "loading" }
    | { status: "loaded"; data: CompanyBackofficeData }
    | { status: "forbidden" }
    | { status: "error"; message: string }
  >({ status: "loading" });
  const [profileDraft, setProfileDraft] = useState<ProfileDraft>({
    companyName: "",
    businessDescription: "",
    contactEmail: "",
    contactPhone: "",
    addressLine1: "",
    addressLine2: "",
    city: "",
    postalCode: "",
    countryCode: ""
  });
  const [profileFeedback, setProfileFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadBackoffice() {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/app/company/${slug}/backoffice`,
          {
            credentials: "include"
          }
        );

        if (!isMounted) {
          return;
        }

        if (response.status === 401) {
          router.replace("/en/login");
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
        setProfileDraft(toProfileDraft(payload));
        setState({ status: "loaded", data: payload });
      } catch {
        if (!isMounted) {
          return;
        }

        setState({ status: "error", message: "The company backoffice could not be loaded." });
      }
    }

    void loadBackoffice();

    return () => {
      isMounted = false;
    };
  }, [router, slug]);

  async function handleProfileSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setProfileFeedback(null);

    const validationMessage = validateProfileDraft(profileDraft);
    if (validationMessage) {
      setProfileFeedback({ type: "error", message: validationMessage });
      return;
    }

    setIsSavingProfile(true);

    try {
      const csrfToken = await fetchCsrfToken();
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/app/company/${slug}/profile`,
        {
          method: "PUT",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
            "X-CSRF-TOKEN": csrfToken
          },
          body: JSON.stringify({
            ...profileDraft,
            businessDescription: profileDraft.businessDescription || null,
            addressLine2: profileDraft.addressLine2 || null,
            countryCode: profileDraft.countryCode.trim().toUpperCase()
          })
        }
      );

      const payload = (await response.json().catch(() => null)) as
        | {
            message?: string;
            company?: CompanyBackofficeData["company"];
            profile?: CompanyBackofficeData["profile"];
          }
        | { message?: string }
        | null;

      if (response.status === 401) {
        router.replace("/en/login");
        setIsSavingProfile(false);
        return;
      }

      if (response.status === 403) {
        setState({ status: "forbidden" });
        setIsSavingProfile(false);
        return;
      }

      if (!response.ok || !payload || !("company" in payload) || !payload.company || !("profile" in payload) || !payload.profile) {
        setProfileFeedback({
          type: "error",
          message: payload?.message ?? "The company profile could not be saved."
        });
        setIsSavingProfile(false);
        return;
      }

      if (state.status === "loaded") {
        const nextData = {
          ...state.data,
          company: payload.company,
          profile: payload.profile
        };

        setState({ status: "loaded", data: nextData });
        setProfileDraft(toProfileDraft(nextData));
      }

      setProfileFeedback({
        type: "success",
        message: payload.message ?? "Company profile updated."
      });
    } catch (error) {
      if (error instanceof CsrfTokenError && error.status === 401) {
        router.replace("/en/login");
        return;
      }

      setProfileFeedback({
        type: "error",
        message: "The company profile could not be saved."
      });
    } finally {
      setIsSavingProfile(false);
    }
  }

  return (
    <Box sx={{ minHeight: "100vh", px: 3, py: 6 }}>
      <Paper
        elevation={0}
        sx={{
          maxWidth: 1180,
          mx: "auto",
          p: { xs: 3, md: 5 },
          border: "1px solid",
          borderColor: "divider",
          background:
            "linear-gradient(180deg, rgba(255, 250, 244, 0.95) 0%, rgba(250, 244, 235, 0.88) 100%)"
        }}
      >
        {state.status === "loading" ? (
          <Stack spacing={2} alignItems="flex-start">
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Stack direction="row" spacing={2} alignItems="center">
              <CircularProgress size={24} />
              <Typography color="text.secondary">Loading company configuration dashboard...</Typography>
            </Stack>
          </Stack>
        ) : null}

        {state.status === "forbidden" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Alert severity="error">Access denied for this company scope.</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "error" ? (
          <Stack spacing={2}>
            <Typography variant="h3" component="h1">
              Company Configuration
            </Typography>
            <Alert severity="error">{state.message}</Alert>
            <LogoutButton />
          </Stack>
        ) : null}

        {state.status === "loaded" ? (
          <Stack spacing={4}>
            <Stack spacing={1.5}>
              <Chip
                label={`${state.data.company.businessType} tenant`}
                sx={{
                  alignSelf: "flex-start",
                  backgroundColor: "rgba(180, 90, 56, 0.10)",
                  color: "primary.dark",
                  fontWeight: 700
                }}
              />
              <Typography variant="h3" component="h1">
                {state.data.company.companyName}
              </Typography>
              <Typography color="text.secondary" sx={{ maxWidth: 760 }}>
                Shared company configuration dashboard for tenant-scoped setup, profile management, and the next
                operational features in Phase 2.
              </Typography>
            </Stack>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, md: 7 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                  <Stack spacing={2.5}>
                    <Typography variant="h5">Company Summary</Typography>
                    <Grid container spacing={2}>
                      {[
                        { label: "Slug", value: state.data.company.companySlug },
                        { label: "Status", value: state.data.company.companyStatus },
                        { label: "Default language", value: state.data.company.defaultLanguage },
                        { label: "Default locale", value: state.data.company.defaultLocale },
                        { label: "Signed in as", value: state.data.viewer.currentUserEmail },
                        { label: "Role", value: state.data.viewer.role }
                      ].map((item) => (
                        <Grid key={item.label} size={{ xs: 12, sm: 6 }}>
                          <Paper
                            elevation={0}
                            sx={{
                              p: 2,
                              height: "100%",
                              borderRadius: 4,
                              backgroundColor: "rgba(255,255,255,0.64)",
                              border: "1px solid rgba(83, 58, 43, 0.10)"
                            }}
                          >
                            <Typography variant="body2" color="text.secondary">
                              {item.label}
                            </Typography>
                            <Typography variant="h6" sx={{ mt: 0.5 }}>
                              {item.value}
                            </Typography>
                          </Paper>
                        </Grid>
                      ))}
                    </Grid>
                  </Stack>
                </Paper>
              </Grid>

              <Grid size={{ xs: 12, md: 5 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                  <Stack spacing={2.5}>
                    <Typography variant="h5">Operational Snapshot</Typography>
                    {[
                      { label: "Plan", value: state.data.operations.planType },
                      { label: "Subscription expires", value: formatUtcDateTime(state.data.operations.subscriptionExpiresAt) },
                      { label: "Staff users", value: String(state.data.operations.staffCount) },
                      { label: "Company admins", value: String(state.data.operations.adminCount) },
                      { label: "Last activity", value: formatUtcDateTime(state.data.operations.lastActivityAt) },
                      { label: "Deletion scheduled", value: formatUtcDateTime(state.data.operations.deletionScheduledAt) }
                    ].map((item) => (
                      <Box
                        key={item.label}
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                          gap: 2,
                          py: 1.25,
                          borderBottom: "1px solid rgba(83, 58, 43, 0.08)"
                        }}
                      >
                        <Typography color="text.secondary">{item.label}</Typography>
                        <Typography sx={{ fontWeight: 700, textAlign: "right" }}>{item.value}</Typography>
                      </Box>
                    ))}
                  </Stack>
                </Paper>
              </Grid>
            </Grid>

            <Grid container spacing={3}>
              <Grid size={{ xs: 12, lg: 8 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3 }}>
                  <Stack spacing={2.5} component="form" onSubmit={handleProfileSubmit}>
                    <Stack spacing={1}>
                      <Typography variant="h4" component="h2">
                        Company Profile
                      </Typography>
                      <Typography color="text.secondary">
                        Manage the profile details that will be reused across the company backoffice and later public
                        booking surfaces.
                      </Typography>
                    </Stack>

                    {profileFeedback ? <Alert severity={profileFeedback.type}>{profileFeedback.message}</Alert> : null}

                    <TextField
                      label="Company name"
                      value={profileDraft.companyName}
                      onChange={(event) => {
                        setProfileDraft((current) => ({ ...current, companyName: event.target.value }));
                      }}
                      fullWidth
                    />
                    <TextField
                      label="Business description"
                      value={profileDraft.businessDescription}
                      onChange={(event) => {
                        setProfileDraft((current) => ({ ...current, businessDescription: event.target.value }));
                      }}
                      multiline
                      minRows={3}
                      fullWidth
                    />
                    <Grid container spacing={2}>
                      <Grid size={{ xs: 12, md: 6 }}>
                        <TextField
                          label="Primary contact email"
                          type="email"
                          value={profileDraft.contactEmail}
                          onChange={(event) => {
                            setProfileDraft((current) => ({ ...current, contactEmail: event.target.value }));
                          }}
                          fullWidth
                        />
                      </Grid>
                      <Grid size={{ xs: 12, md: 6 }}>
                        <TextField
                          label="Primary contact phone"
                          value={profileDraft.contactPhone}
                          onChange={(event) => {
                            setProfileDraft((current) => ({ ...current, contactPhone: event.target.value }));
                          }}
                          fullWidth
                        />
                      </Grid>
                    </Grid>
                    <TextField
                      label="Address line 1"
                      value={profileDraft.addressLine1}
                      onChange={(event) => {
                        setProfileDraft((current) => ({ ...current, addressLine1: event.target.value }));
                      }}
                      fullWidth
                    />
                    <TextField
                      label="Address line 2"
                      value={profileDraft.addressLine2}
                      onChange={(event) => {
                        setProfileDraft((current) => ({ ...current, addressLine2: event.target.value }));
                      }}
                      fullWidth
                    />
                    <Grid container spacing={2}>
                      <Grid size={{ xs: 12, md: 4 }}>
                        <TextField
                          label="City"
                          value={profileDraft.city}
                          onChange={(event) => {
                            setProfileDraft((current) => ({ ...current, city: event.target.value }));
                          }}
                          fullWidth
                        />
                      </Grid>
                      <Grid size={{ xs: 12, md: 4 }}>
                        <TextField
                          label="Postal code"
                          value={profileDraft.postalCode}
                          onChange={(event) => {
                            setProfileDraft((current) => ({ ...current, postalCode: event.target.value }));
                          }}
                          fullWidth
                        />
                      </Grid>
                      <Grid size={{ xs: 12, md: 4 }}>
                        <TextField
                          label="Country code"
                          value={profileDraft.countryCode}
                          onChange={(event) => {
                            setProfileDraft((current) => ({ ...current, countryCode: event.target.value.toUpperCase() }));
                          }}
                          inputProps={{ maxLength: 2 }}
                          fullWidth
                        />
                      </Grid>
                    </Grid>

                    <Box>
                      <Button type="submit" variant="contained" disabled={isSavingProfile}>
                        {isSavingProfile ? "Saving profile..." : "Save company profile"}
                      </Button>
                    </Box>
                  </Stack>
                </Paper>
              </Grid>

              <Grid size={{ xs: 12, lg: 4 }}>
                <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                  <Stack spacing={2}>
                    <Typography variant="h5">Profile Readiness</Typography>
                    <Typography color="text.secondary">
                      A complete company profile gives the next Phase 2 settings a reliable operational baseline.
                    </Typography>
                    {[
                      { label: "Business description", value: state.data.profile.businessDescription ? "Configured" : "Missing" },
                      { label: "Contact details", value: state.data.profile.contactEmail && state.data.profile.contactPhone ? "Configured" : "Missing" },
                      { label: "Address", value: state.data.profile.addressLine1 && state.data.profile.city ? "Configured" : "Missing" }
                    ].map((item) => (
                      <Box
                        key={item.label}
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                          gap: 2,
                          py: 1.25,
                          borderBottom: "1px solid rgba(83, 58, 43, 0.08)"
                        }}
                      >
                        <Typography color="text.secondary">{item.label}</Typography>
                        <Typography sx={{ fontWeight: 700 }}>{item.value}</Typography>
                      </Box>
                    ))}
                  </Stack>
                </Paper>
              </Grid>
            </Grid>

            <Stack spacing={2}>
              <Typography variant="h4" component="h2">
                Shared Configuration Areas
              </Typography>
              <Typography color="text.secondary">
                These sections define the Phase 2 backoffice baseline. Available items can be implemented next without
                changing the tenant navigation model.
              </Typography>
              <Grid container spacing={2.5}>
                {state.data.configurationAreas.map((area) => (
                  <Grid key={area.key} size={{ xs: 12, md: 6, xl: 4 }}>
                    <Paper elevation={0} variant="outlined" sx={{ p: 3, height: "100%" }}>
                      <Stack spacing={1.5}>
                        <Chip
                          label={area.status === "available" ? "Available now" : "Planned next"}
                          size="small"
                          sx={{
                            alignSelf: "flex-start",
                            backgroundColor:
                              area.status === "available" ? "rgba(53, 95, 89, 0.12)" : "rgba(212, 154, 82, 0.18)",
                            color: area.status === "available" ? "secondary.main" : "primary.dark",
                            fontWeight: 700
                          }}
                        />
                        <Typography variant="h6">{area.title}</Typography>
                        <Typography color="text.secondary">{area.description}</Typography>
                      </Stack>
                    </Paper>
                  </Grid>
                ))}
              </Grid>
            </Stack>

            <LogoutButton />
          </Stack>
        ) : null}
      </Paper>
    </Box>
  );
}
