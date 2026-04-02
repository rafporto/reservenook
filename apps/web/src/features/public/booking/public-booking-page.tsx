"use client";

import { useEffect, useMemo, useState } from "react";
import { Alert, Box, Button, Paper, Stack, TextField, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";

type Props = {
  locale: SupportedLocale;
  slug: string;
  widgetToken?: string;
  embedded?: boolean;
  themeVariant?: string;
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
  classTypes: Array<{
    id: number;
    name: string;
    description: string | null;
    durationMinutes: number;
    defaultCapacity: number;
  }>;
};

type PublicAppointmentSlot = {
  serviceId: number;
  providerId: number;
  providerName: string;
  startsAt: string;
  endsAt: string;
};

type PublicClassSession = {
  sessionId: number;
  classTypeId: number;
  classTypeName: string;
  instructorId: number;
  instructorName: string;
  startsAt: string;
  endsAt: string;
  remainingCapacity: number;
  waitlistOpen: boolean;
};

type PublicRestaurantSlot = {
  startsAt: string;
  servicePeriodId: number;
  servicePeriodName: string;
  partySize: number;
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
    loadingClasses: "Checking class availability...",
    loadingRestaurant: "Checking table availability...",
    chooseSlot: "Choose a time slot",
    classType: "Class type",
    partySize: "Party size",
    reservationDate: "Reservation date",
    availableTables: "Available reservation times",
    noRestaurantSlots: "No restaurant tables are available for the selected date and party size.",
    restaurantSubmit: "Reserve table",
    restaurantSuccess: "Your restaurant reservation has been received.",
    invalidRestaurant: "Please provide a valid full name, email address, party size, date, and reservation time.",
    availableClasses: "Upcoming classes",
    noClasses: "No class sessions are available right now.",
    classSubmit: "Book class",
    classSuccess: "Your class booking request has been received.",
    invalidClass: "Please provide a valid full name, email address, class type, and session.",
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
    loadingClasses: "Kursverfugbarkeit wird gepruft...",
    loadingRestaurant: "Tischverfugbarkeit wird gepruft...",
    chooseSlot: "Zeitfenster auswahlen",
    classType: "Kurstyp",
    partySize: "Personenzahl",
    reservationDate: "Reservierungsdatum",
    availableTables: "Verfugbare Reservierungszeiten",
    noRestaurantSlots: "Fur Datum und Personenzahl sind keine Tische verfugbar.",
    restaurantSubmit: "Tisch reservieren",
    restaurantSuccess: "Ihre Restaurantreservierung wurde empfangen.",
    invalidRestaurant: "Bitte geben Sie einen gultigen Namen, eine gultige E-Mail-Adresse, Personenzahl, Datum und Reservierungszeit an.",
    availableClasses: "Kommende Kurse",
    noClasses: "Derzeit sind keine Kurstermine verfugbar.",
    classSubmit: "Kurs buchen",
    classSuccess: "Ihre Kursbuchung wurde empfangen.",
    invalidClass: "Bitte geben Sie einen gultigen Namen, eine gultige E-Mail-Adresse, einen Kurstyp und einen Termin an.",
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
    loadingClasses: "A verificar turmas disponiveis...",
    loadingRestaurant: "A verificar disponibilidade de mesas...",
    chooseSlot: "Escolha um horario",
    classType: "Tipo de aula",
    partySize: "Numero de pessoas",
    reservationDate: "Data da reserva",
    availableTables: "Horarios disponiveis para reserva",
    noRestaurantSlots: "Nao ha mesas disponiveis para a data e tamanho do grupo selecionados.",
    restaurantSubmit: "Reservar mesa",
    restaurantSuccess: "A sua reserva de restaurante foi recebida.",
    invalidRestaurant: "Indique um nome, email, numero de pessoas, data e horario de reserva validos.",
    availableClasses: "Aulas disponiveis",
    noClasses: "Nao ha aulas disponiveis neste momento.",
    classSubmit: "Reservar aula",
    classSuccess: "O seu pedido de reserva para a aula foi recebido.",
    invalidClass: "Indique um nome, email, tipo de aula e sessao validos.",
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

export function PublicBookingPage({ locale, slug, widgetToken, embedded = false, themeVariant = "minimal" }: Props) {
  const messages = copy[locale];
  const [config, setConfig] = useState<PublicBookingConfig | null>(null);
  const [state, setState] = useState<"loading" | "ready" | "unavailable">("loading");
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; message: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slots, setSlots] = useState<PublicAppointmentSlot[]>([]);
  const [loadingClasses, setLoadingClasses] = useState(false);
  const [classSessions, setClassSessions] = useState<PublicClassSession[]>([]);
  const [loadingRestaurant, setLoadingRestaurant] = useState(false);
  const [restaurantSlots, setRestaurantSlots] = useState<PublicRestaurantSlot[]>([]);
  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    requestSummary: "",
    preferredDate: "",
    notes: "",
    serviceId: "",
    appointmentDate: "",
    selectedSlotStartsAt: "",
    classTypeId: "",
    selectedClassSessionId: "",
    restaurantDate: "",
    partySize: "",
    selectedRestaurantStartsAt: ""
  });

  const isAppointmentFlow = useMemo(
    () => config?.businessType === "APPOINTMENT" && (config.appointmentServices?.length ?? 0) > 0,
    [config]
  );
  const isClassFlow = useMemo(
    () => config?.businessType === "CLASS" && (config.classTypes?.length ?? 0) > 0,
    [config]
  );
  const isRestaurantFlow = useMemo(
    () => config?.businessType === "RESTAURANT",
    [config]
  );

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/booking-intake-config`, {
          credentials: "include",
          headers: widgetToken ? { "X-ReserveNook-Widget-Token": widgetToken } : undefined
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
  }, [slug, widgetToken]);

  useEffect(() => {
    if (!isClassFlow || !form.classTypeId) {
      setClassSessions([]);
      return;
    }
    let active = true;
    setLoadingClasses(true);
    (async () => {
      try {
        const params = new URLSearchParams({ classTypeId: form.classTypeId });
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/classes/availability?${params.toString()}`, {
          credentials: "include",
          headers: widgetToken ? { "X-ReserveNook-Widget-Token": widgetToken } : undefined
        });
        if (!active) return;
        if (!response.ok) {
          setClassSessions([]);
          return;
        }
        const payload = (await response.json()) as { sessions: PublicClassSession[] };
        setClassSessions(payload.sessions);
      } catch {
        if (active) {
          setClassSessions([]);
        }
      } finally {
        if (active) {
          setLoadingClasses(false);
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [form.classTypeId, isClassFlow, slug, widgetToken]);

  useEffect(() => {
    if (!isRestaurantFlow || !form.restaurantDate || !form.partySize) {
      setRestaurantSlots([]);
      return;
    }
    let active = true;
    setLoadingRestaurant(true);
    (async () => {
      try {
        const params = new URLSearchParams({
          date: form.restaurantDate,
          partySize: form.partySize
        });
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/${slug}/restaurant/availability?${params.toString()}`, {
          credentials: "include",
          headers: widgetToken ? { "X-ReserveNook-Widget-Token": widgetToken } : undefined
        });
        if (!active) return;
        if (!response.ok) {
          setRestaurantSlots([]);
          return;
        }
        const payload = (await response.json()) as { slots: PublicRestaurantSlot[] };
        setRestaurantSlots(payload.slots);
      } catch {
        if (active) {
          setRestaurantSlots([]);
        }
      } finally {
        if (active) {
          setLoadingRestaurant(false);
        }
      }
    })();
    return () => {
      active = false;
    };
  }, [form.partySize, form.restaurantDate, isRestaurantFlow, slug, widgetToken]);

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
          credentials: "include",
          headers: widgetToken ? { "X-ReserveNook-Widget-Token": widgetToken } : undefined
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
  }, [form.appointmentDate, form.serviceId, isAppointmentFlow, slug, widgetToken]);

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
        maxWidth: embedded ? "100%" : 760,
        mx: "auto",
        borderRadius: embedded ? 3 : 6,
        border: "1px solid rgba(83,58,43,0.12)",
        p: embedded ? { xs: 2.5, md: 3 } : { xs: 3, md: 5 },
        background: themeVariant === "contrast"
          ? "linear-gradient(180deg, rgba(244,236,225,0.98) 0%, rgba(232,218,202,0.95) 100%)"
          : themeVariant === "soft"
            ? "linear-gradient(180deg, rgba(255,248,240,0.98) 0%, rgba(250,242,233,0.94) 100%)"
            : "linear-gradient(180deg, rgba(255,252,249,0.98) 0%, rgba(248,244,238,0.94) 100%)",
        boxShadow: embedded ? "none" : undefined
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
          if (isClassFlow) {
            const selectedSession = classSessions.find((session) => String(session.sessionId) === form.selectedClassSessionId);
            if (!form.classTypeId || selectedSession == null) {
              setFeedback({ type: "error", message: messages.invalidClass });
              return;
            }
          }
          if (isRestaurantFlow) {
            const selectedSlot = restaurantSlots.find((slot) => slot.startsAt === form.selectedRestaurantStartsAt);
            if (!form.partySize || !form.restaurantDate || selectedSlot == null) {
              setFeedback({ type: "error", message: messages.invalidRestaurant });
              return;
            }
          }

          setSubmitting(true);
          try {
            const endpoint = isAppointmentFlow
              ? `/api/public/companies/${slug}/appointments/book`
              : isClassFlow
                ? `/api/public/companies/${slug}/classes/book`
              : isRestaurantFlow
                ? `/api/public/companies/${slug}/restaurant/book`
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
              : isClassFlow
                ? {
                    fullName: form.fullName.trim(),
                    email: form.email.trim(),
                    phone: form.phone.trim() || null,
                    preferredLanguage: locale,
                    sessionId: Number(form.selectedClassSessionId)
                  }
              : isRestaurantFlow
                ? {
                    fullName: form.fullName.trim(),
                    email: form.email.trim(),
                    phone: form.phone.trim() || null,
                    preferredLanguage: locale,
                    partySize: Number(form.partySize),
                    startsAt: form.selectedRestaurantStartsAt
                  }
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
              headers: { "Content-Type": "application/json", ...(widgetToken ? { "X-ReserveNook-Widget-Token": widgetToken } : {}) },
              body: JSON.stringify(body)
            });
            const payload = (await response.json().catch(() => null)) as { message?: string } | null;
            if (!response.ok) {
              setFeedback({ type: "error", message: payload?.message ?? messages.unavailable });
              return;
            }
            setFeedback({
              type: "success",
              message: payload?.message ?? (isAppointmentFlow ? messages.appointmentSuccess : isClassFlow ? messages.classSuccess : isRestaurantFlow ? messages.restaurantSuccess : messages.success)
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
              selectedSlotStartsAt: "",
              classTypeId: "",
              selectedClassSessionId: "",
              restaurantDate: "",
              partySize: "",
              selectedRestaurantStartsAt: ""
            });
            setSlots([]);
            setClassSessions([]);
            setRestaurantSlots([]);
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
          ) : isClassFlow ? (
            <>
              <TextField label={messages.classType} value={form.classTypeId} onChange={(event) => setForm((current) => ({
                ...current,
                classTypeId: event.target.value,
                selectedClassSessionId: ""
              }))} select fullWidth SelectProps={{ native: true }}>
                <option value=""></option>
                {config.classTypes.map((classType) => (
                  <option key={classType.id} value={String(classType.id)}>
                    {classType.name} ({classType.durationMinutes} min)
                  </option>
                ))}
              </TextField>
              <Stack spacing={1}>
                <Typography variant="subtitle1">{messages.availableClasses}</Typography>
                {loadingClasses ? <Typography color="text.secondary">{messages.loadingClasses}</Typography> : null}
                {!loadingClasses && form.classTypeId && classSessions.length === 0 ? (
                  <Typography color="text.secondary">{messages.noClasses}</Typography>
                ) : null}
                <Stack spacing={1}>
                  {classSessions.map((session) => (
                    <Button
                      key={session.sessionId}
                      type="button"
                      variant={form.selectedClassSessionId === String(session.sessionId) ? "contained" : "outlined"}
                      onClick={() => setForm((current) => ({ ...current, selectedClassSessionId: String(session.sessionId) }))}
                    >
                      {new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short", timeZone: "UTC" }).format(new Date(session.startsAt))}
                      {" · "}
                      {session.instructorName}
                      {" · "}
                      {session.remainingCapacity > 0 ? `${session.remainingCapacity} left` : "Waitlist"}
                    </Button>
                  ))}
                </Stack>
              </Stack>
            </>
          ) : isRestaurantFlow ? (
            <>
              <TextField label={messages.partySize} value={form.partySize} onChange={(event) => setForm((current) => ({
                ...current,
                partySize: event.target.value,
                selectedRestaurantStartsAt: ""
              }))} fullWidth />
              <TextField label={messages.reservationDate} type="date" value={form.restaurantDate} onChange={(event) => setForm((current) => ({
                ...current,
                restaurantDate: event.target.value,
                selectedRestaurantStartsAt: ""
              }))} InputLabelProps={{ shrink: true }} fullWidth />
              <Stack spacing={1}>
                <Typography variant="subtitle1">{messages.availableTables}</Typography>
                {loadingRestaurant ? <Typography color="text.secondary">{messages.loadingRestaurant}</Typography> : null}
                {!loadingRestaurant && form.restaurantDate && form.partySize && restaurantSlots.length === 0 ? (
                  <Typography color="text.secondary">{messages.noRestaurantSlots}</Typography>
                ) : null}
                <Stack direction="row" flexWrap="wrap" gap={1}>
                  {restaurantSlots.map((slot) => (
                    <Button
                      key={`${slot.servicePeriodId}-${slot.startsAt}`}
                      type="button"
                      variant={form.selectedRestaurantStartsAt === slot.startsAt ? "contained" : "outlined"}
                      onClick={() => setForm((current) => ({ ...current, selectedRestaurantStartsAt: slot.startsAt }))}
                    >
                      {new Intl.DateTimeFormat(locale, { dateStyle: "medium", timeStyle: "short", timeZone: "UTC" }).format(new Date(slot.startsAt))}
                      {" · "}
                      {slot.servicePeriodName}
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

          <Button type="submit" variant="contained" disabled={submitting || (isAppointmentFlow && loadingSlots) || (isClassFlow && loadingClasses) || (isRestaurantFlow && loadingRestaurant)}>
            {submitting ? messages.submitting : (isAppointmentFlow ? messages.appointmentSubmit : isClassFlow ? messages.classSubmit : isRestaurantFlow ? messages.restaurantSubmit : (config.ctaLabel || messages.submit))}
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
}
