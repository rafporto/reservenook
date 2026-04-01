"use client";

import { useEffect, useState } from "react";
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";

type Props = {
  locale: SupportedLocale;
  slug: string;
};

type PublicBookingConfig = {
  companyName: string;
  companySlug: string;
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
};

const copy: Record<SupportedLocale, Record<string, string>> = {
  en: {
    loading: "Loading booking request form...",
    unavailable: "This booking page is currently unavailable.",
    title: "Request a booking",
    description: "Share the booking details below. The company will review your request and contact you.",
    fullName: "Full name",
    email: "Email",
    phone: "Phone",
    summary: "Request summary",
    preferredDate: "Preferred date",
    notes: "Additional notes",
    submit: "Send booking request",
    submitting: "Sending request...",
    success: "Your booking request has been received.",
    invalid: "Please provide a valid full name and email address."
  },
  de: {
    loading: "Buchungsformular wird geladen...",
    unavailable: "Diese Buchungsseite ist derzeit nicht verfugbar.",
    title: "Buchung anfragen",
    description: "Teilen Sie die Buchungsdetails unten mit. Das Unternehmen pruft Ihre Anfrage und meldet sich bei Ihnen.",
    fullName: "Vollstandiger Name",
    email: "E-Mail",
    phone: "Telefon",
    summary: "Anfragezusammenfassung",
    preferredDate: "Wunschter Termin",
    notes: "Zusatzliche Hinweise",
    submit: "Buchungsanfrage senden",
    submitting: "Anfrage wird gesendet...",
    success: "Ihre Buchungsanfrage wurde empfangen.",
    invalid: "Bitte geben Sie einen gultigen Namen und eine gultige E-Mail-Adresse ein."
  },
  pt: {
    loading: "A carregar o formulario de reserva...",
    unavailable: "Esta pagina de reserva nao esta disponivel de momento.",
    title: "Pedir reserva",
    description: "Partilhe os detalhes abaixo. A empresa vai rever o pedido e entrar em contacto consigo.",
    fullName: "Nome completo",
    email: "Email",
    phone: "Telefone",
    summary: "Resumo do pedido",
    preferredDate: "Data pretendida",
    notes: "Notas adicionais",
    submit: "Enviar pedido de reserva",
    submitting: "A enviar pedido...",
    success: "O seu pedido de reserva foi recebido.",
    invalid: "Indique um nome e um email validos."
  }
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function PublicBookingPage({ locale, slug }: Props) {
  const messages = copy[locale];
  const [config, setConfig] = useState<PublicBookingConfig | null>(null);
  const [state, setState] = useState<"loading" | "ready" | "unavailable">("loading");
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    requestSummary: "",
    preferredDate: "",
    notes: ""
  });

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
          <Typography variant="h3">{messages.title}</Typography>
          <Typography color="text.secondary">{messages.description}</Typography>
        </Box>
        {feedback ? <Alert severity={feedback.type}>{feedback.message}</Alert> : null}
        <Stack spacing={2} component="form" onSubmit={async (event) => {
          event.preventDefault();
          setFeedback(null);
          if (!form.fullName.trim() || !emailPattern.test(form.email)) {
            setFeedback({ type: "error", message: messages.invalid });
            return;
          }
          setSubmitting(true);
          try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/booking-intake`, {
              method: "POST",
              credentials: "include",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({
                fullName: form.fullName.trim(),
                email: form.email.trim(),
                phone: form.phone.trim() || null,
                preferredLanguage: locale,
                requestSummary: form.requestSummary.trim() || null,
                preferredDate: form.preferredDate || null,
                notes: form.notes.trim() || null
              })
            });
            const payload = (await response.json().catch(() => null)) as { message?: string } | null;
            if (!response.ok) {
              setFeedback({ type: "error", message: payload?.message ?? messages.unavailable });
              return;
            }
            setFeedback({ type: "success", message: payload?.message ?? messages.success });
            setForm({ fullName: "", email: "", phone: "", requestSummary: "", preferredDate: "", notes: "" });
          } catch {
            setFeedback({ type: "error", message: messages.unavailable });
          } finally {
            setSubmitting(false);
          }
        }}>
          <TextField label={messages.fullName} value={form.fullName} onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))} fullWidth />
          <TextField label={messages.email} value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} fullWidth />
          <TextField label={messages.phone} value={form.phone} onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))} fullWidth />
          <TextField label={messages.summary} value={form.requestSummary} onChange={(event) => setForm((current) => ({ ...current, requestSummary: event.target.value }))} fullWidth />
          <TextField label={messages.preferredDate} type="date" value={form.preferredDate} onChange={(event) => setForm((current) => ({ ...current, preferredDate: event.target.value }))} InputLabelProps={{ shrink: true }} fullWidth />
          <TextField label={messages.notes} value={form.notes} onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))} multiline minRows={4} fullWidth />
          {config.customerQuestions.map((question) => (
            <Typography key={question.label} color="text.secondary">
              {question.required ? "* " : ""}{question.label}
            </Typography>
          ))}
          <Button type="submit" variant="contained" disabled={submitting}>
            {submitting ? messages.submitting : (config.ctaLabel || messages.submit)}
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
}
