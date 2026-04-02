"use client";

import { useEffect, useMemo, useState } from "react";
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";

type Props = {
  locale: SupportedLocale;
  slug: string;
};

type PublicBookingConfig = {
  companyName: string;
  companySlug: string;
  businessType: string;
  displayName: string;
  defaultLanguage: string;
  defaultLocale: string;
  ctaLabel: string;
  bookingEnabled: boolean;
  customerQuestions: Array<{
    label: string;
    questionType: string;
    required: boolean;
    options: string[];
  }>;
  appointmentServices: Array<{
    id: number;
    name: string;
    description: string | null;
    durationMinutes: number;
    priceLabel: string | null;
  }>;
};

type PublicAppointmentSlot = {
  serviceId: number;
  providerId: number;
  providerName: string;
  startsAt: string;
  endsAt: string;
};

const copy: Record<SupportedLocale, Record<string, string>> = {
  en: {
    loading: "Loading booking request form...",
    unavailable: "This booking page is currently unavailable.",
    title: "Request a booking",
    description: "Share the booking details below. The company will review your request and contact you.",
    appointmentTitle: "Book an appointment",
    appointmentDescription: "Choose a service, pick an available slot, and submit your appointment request.",
    fullName: "Full name",
    email: "Email",
    phone: "Phone",
    summary: "Request summary",
    preferredDate: "Preferred date",
    notes: "Additional notes",
    service: "Service",
    slotDate: "Appointment date",
    availableSlots: "Available slots",
    noSlots: "No appointment slots are available for the selected date.",
    loadingSlots: "Checking appointment availability...",
    chooseSlot: "Choose a time slot",
    submit: "Send booking request",
    appointmentSubmit: "Book appointment",
    submitting: "Sending request...",
    success: "Your booking request has been received.",
    appointmentSuccess: "Your appointment request has been received.",
    invalid: "Please provide a valid full name and email address.",
    invalidAppointment: "Please provide a valid full name, email address, service, date, and time slot."
  },
  de: {
    loading: "Buchungsformular wird geladen...",
    unavailable: "Diese Buchungsseite ist derzeit nicht verfugbar.",
    title: "Buchung anfragen",
    description: "Teilen Sie die Buchungsdetails unten mit. Das Unternehmen pruft Ihre Anfrage und meldet sich bei Ihnen.",
    appointmentTitle: "Termin buchen",
    appointmentDescription: "Wahlen Sie eine Leistung, dann einen verfugbaren Termin, und senden Sie Ihre Anfrage.",
    fullName: "Vollstandiger Name",
    email: "E-Mail",
    phone: "Telefon",
    summary: "Anfragezusammenfassung",
    preferredDate: "Wunschter Termin",
    notes: "Zusatzliche Hinweise",
    service: "Leistung",
    slotDate: "Termindatum",
    availableSlots: "Verfugbare Zeitfenster",
    noSlots: "Fur das gewahlte Datum sind keine Termine verfugbar.",
    loadingSlots: "Terminverfugbarkeit wird gepruft...",
    chooseSlot: "Zeitfenster auswahlen",
    submit: "Buchungsanfrage senden",
    appointmentSubmit: "Termin buchen",
    submitting: "Anfrage wird gesendet...",
    success: "Ihre Buchungsanfrage wurde empfangen.",
    appointmentSuccess: "Ihre Terminanfrage wurde empfangen.",
    invalid: "Bitte geben Sie einen gultigen Namen und eine gultige E-Mail-Adresse ein.",
    invalidAppointment: "Bitte geben Sie einen gultigen Namen, eine gultige E-Mail-Adresse, eine Leistung, ein Datum und ein Zeitfenster an."
  },
  pt: {
    loading: "A carregar o formulario de reserva...",
    unavailable: "Esta pagina de reserva nao esta disponivel de momento.",
    title: "Pedir reserva",
    description: "Partilhe os detalhes abaixo. A empresa vai rever o pedido e entrar em contacto consigo.",
    appointmentTitle: "Marcar atendimento",
    appointmentDescription: "Escolha um servico, selecione uma vaga disponivel e envie o seu pedido de marcacao.",
    fullName: "Nome completo",
    email: "Email",
    phone: "Telefone",
    summary: "Resumo do pedido",
    preferredDate: "Data pretendida",
    notes: "Notas adicionais",
    service: "Servico",
    slotDate: "Data do atendimento",
    availableSlots: "Horarios disponiveis",
    noSlots: "Nao ha horarios disponiveis para a data selecionada.",
    loadingSlots: "A verificar disponibilidade...",
    chooseSlot: "Escolha um horario",
    submit: "Enviar pedido de reserva",
    appointmentSubmit: "Marcar atendimento",
    submitting: "A enviar pedido...",
    success: "O seu pedido de reserva foi recebido.",
    appointmentSuccess: "O seu pedido de marcacao foi recebido.",
    invalid: "Indique um nome e um email validos.",
    invalidAppointment: "Indique um nome, email, servico, data e horario validos."
  }
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function formatSlotLabel(slot: PublicAppointmentSlot, locale: SupportedLocale) {
  return `${new Intl.DateTimeFormat(locale, {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "UTC"
  }).format(new Date(slot.startsAt))} - ${slot.providerName}`;
}

export function PublicBookingPage({ locale, slug }: Props) {
  const messages = copy[locale];
  const [config, setConfig] = useState<PublicBookingConfig | null>(null);
  const [state, setState] = useState<"loading" | "ready" | "unavailable">("loading");
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slots, setSlots] = useState<PublicAppointmentSlot[]>([]);
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    requestSummary: "",
    preferredDate: "",
    notes: "",
    serviceId: "",
    appointmentDate: "",
    selectedSlotStartsAt: ""
  });

  const isAppointmentFlow = useMemo(
    () => config?.businessType === "APPOINTMENT" && (config.appointmentServices?.length ?? 0) > 0,
    [config]
  );

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/booking-intake-config`, {
          credentials: "include"
        });
        if (!active) return;
        if (!response.ok) {
          setState("unavailable");
          return;
        }
        const payload = (await response.json()) as PublicBookingConfig;
        setConfig(payload);
        setState("ready");
      } catch {
        if (active) {
          setState("unavailable");
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [slug]);

  useEffect(() => {
    if (!isAppointmentFlow || !form.serviceId || !form.appointmentDate) {
      setSlots([]);
      return;
    }
    let active = true;
    setLoadingSlots(true);
    setFeedback(null);
    (async () => {
      try {
        const params = new URLSearchParams({
          serviceId: form.serviceId,
          date: form.appointmentDate
        });
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/appointments/availability?${params.toString()}`, {
          credentials: "include"
        });
        if (!active) return;
        if (!response.ok) {
          setSlots([]);
          return;
        }
        const payload = (await response.json()) as { slots: PublicAppointmentSlot[] };
        setSlots(payload.slots);
      } catch {
        if (active) {
          setSlots([]);
        }
      } finally {
        if (active) {
          setLoadingSlots(false);
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [form.appointmentDate, form.serviceId, isAppointmentFlow, slug]);

  if (state === "loading") {
    return <Typography color="text.secondary">{messages.loading}</Typography>;
  }

  if (state === "unavailable" || config == null) {
    return <Alert severity="error">{messages.unavailable}</Alert>;
  }

  return (
    <Paper
      elevation={0}
      sx={{
        maxWidth: 760,
        mx: "auto",
        borderRadius: 6,
        border: "1px solid rgba(83,58,43,0.12)",
        p: { xs: 3, md: 5 },
        background: "linear-gradient(180deg, rgba(255,248,240,0.98) 0%, rgba(250,242,233,0.94) 100%)"
      }}
    >
      <Stack spacing={3}>
        <Box>
          <Typography variant="overline" color="text.secondary">{config.displayName}</Typography>
          <Typography variant="h3">{isAppointmentFlow ? messages.appointmentTitle : messages.title}</Typography>
          <Typography color="text.secondary">{isAppointmentFlow ? messages.appointmentDescription : messages.description}</Typography>
        </Box>
        {feedback ? <Alert severity={feedback.type}>{feedback.message}</Alert> : null}
        <Stack spacing={2} component="form" onSubmit={async (event) => {
          event.preventDefault();
          setFeedback(null);
          if (!form.fullName.trim() || !emailPattern.test(form.email.trim())) {
            setFeedback({ type: "error", message: messages.invalid });
            return;
          }
          if (isAppointmentFlow) {
            const selectedSlot = slots.find((slot) => slot.startsAt === form.selectedSlotStartsAt);
            if (!form.serviceId || !form.appointmentDate || selectedSlot == null) {
              setFeedback({ type: "error", message: messages.invalidAppointment });
              return;
            }
          }

          setSubmitting(true);
          try {
            const endpoint = isAppointmentFlow
              ? `/api/public/companies/${slug}/appointments/book`
              : `/api/public/companies/${slug}/booking-intake`;
            const body = isAppointmentFlow
              ? (() => {
                  const selectedSlot = slots.find((slot) => slot.startsAt === form.selectedSlotStartsAt);
                  return {
                    fullName: form.fullName.trim(),
                    email: form.email.trim(),
                    phone: form.phone.trim() || null,
                    preferredLanguage: locale,
                    serviceId: Number(form.serviceId),
                    providerId: selectedSlot?.providerId,
                    startsAt: selectedSlot?.startsAt
                  };
                })()
              : {
                  fullName: form.fullName.trim(),
                  email: form.email.trim(),
                  phone: form.phone.trim() || null,
                  preferredLanguage: locale,
                  requestSummary: form.requestSummary.trim() || null,
                  preferredDate: form.preferredDate || null,
                  notes: form.notes.trim() || null
                };
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}${endpoint}`, {
              method: "POST",
              credentials: "include",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(body)
            });
            const payload = (await response.json().catch(() => null)) as { message?: string } | null;
            if (!response.ok) {
              setFeedback({ type: "error", message: payload?.message ?? messages.unavailable });
              return;
            }
            setFeedback({
              type: "success",
              message: payload?.message ?? (isAppointmentFlow ? messages.appointmentSuccess : messages.success)
            });
            setForm({
              fullName: "",
              email: "",
              phone: "",
              requestSummary: "",
              preferredDate: "",
              notes: "",
              serviceId: "",
              appointmentDate: "",
              selectedSlotStartsAt: ""
            });
            setSlots([]);
          } catch {
            setFeedback({ type: "error", message: messages.unavailable });
          } finally {
            setSubmitting(false);
          }
        }}>
          <TextField label={messages.fullName} value={form.fullName} onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))} fullWidth />
          <TextField label={messages.email} value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} fullWidth />
          <TextField label={messages.phone} value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} fullWidth />

          {isAppointmentFlow ? (
            <>
              <TextField label={messages.service} value={form.serviceId} onChange={(event) => setForm((current) => ({
                ...current,
                serviceId: event.target.value,
                selectedSlotStartsAt: ""
              }))} select fullWidth SelectProps={{ native: true }}>
                <option value=""></option>
                {config.appointmentServices.map((service) => (
                  <option key={service.id} value={String(service.id)}>
                    {service.name} ({service.durationMinutes} min{service.priceLabel ? ` - ${service.priceLabel}` : ""})
                  </option>
                ))}
              </TextField>
              <TextField label={messages.slotDate} type="date" value={form.appointmentDate} onChange={(event) => setForm((current) => ({
                ...current,
                appointmentDate: event.target.value,
                selectedSlotStartsAt: ""
              }))} InputLabelProps={{ shrink: true }} fullWidth />
              <Stack spacing={1}>
                <Typography variant="subtitle1">{messages.availableSlots}</Typography>
                {loadingSlots ? <Typography color="text.secondary">{messages.loadingSlots}</Typography> : null}
                {!loadingSlots && form.serviceId && form.appointmentDate && slots.length === 0 ? (
                  <Typography color="text.secondary">{messages.noSlots}</Typography>
                ) : null}
                <Stack direction="row" flexWrap="wrap" gap={1}>
                  {slots.map((slot) => (
                    <Button
                      key={`${slot.providerId}-${slot.startsAt}`}
                      type="button"
                      variant={form.selectedSlotStartsAt === slot.startsAt ? "contained" : "outlined"}
                      onClick={() => setForm((current) => ({ ...current, selectedSlotStartsAt: slot.startsAt }))}
                    >
                      {formatSlotLabel(slot, locale)}
                    </Button>
                  ))}
                </Stack>
              </Stack>
            </>
          ) : (
            <>
              <TextField label={messages.summary} value={form.requestSummary} onChange={(event) => setForm((current) => ({ ...current, requestSummary: event.target.value }))} fullWidth />
              <TextField label={messages.preferredDate} type="date" value={form.preferredDate} onChange={(event) => setForm((current) => ({ ...current, preferredDate: event.target.value }))} InputLabelProps={{ shrink: true }} fullWidth />
              <TextField label={messages.notes} value={form.notes} onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))} multiline minRows={4} fullWidth />
              {config.customerQuestions.map((question) => (
                <Typography key={question.label} color="text.secondary">
                  {question.required ? "* " : ""}{question.label}
                </Typography>
              ))}
            </>
          )}

          <Button type="submit" variant="contained" disabled={submitting || (isAppointmentFlow && loadingSlots)}>
            {submitting ? messages.submitting : (isAppointmentFlow ? messages.appointmentSubmit : (config.ctaLabel || messages.submit))}
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
}
