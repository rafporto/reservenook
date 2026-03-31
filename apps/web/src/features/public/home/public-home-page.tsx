import Link from "next/link";
import { Box, Button, Chip, Divider, Grid, Paper, Stack, Typography } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type PublicHomePageProps = {
  locale: SupportedLocale;
};

const localizedSections: Record<
  SupportedLocale,
  {
    trustLabel: string;
    heroKicker: string[];
    metrics: Array<{ label: string; value: string }>;
    outcomesTitle: string;
    outcomes: string[];
    processTitle: string;
    process: Array<{ step: string; title: string; description: string }>;
    faqTitle: string;
    faq: Array<{ question: string; answer: string }>;
    secondaryCta: string;
  }
> = {
  en: {
    trustLabel: "Designed for service businesses with complex reservations",
    heroKicker: ["Appointments", "Classes", "Restaurant reservations"],
    metrics: [
      { label: "Shared onboarding", value: "One account model" },
      { label: "Localized experience", value: "3 public languages" },
      { label: "Operational control", value: "Platform-admin lifecycle policies" }
    ],
    outcomesTitle: "A cleaner booking experience for every business type",
    outcomes: [
      "Launch one company account with the right booking flow from day one.",
      "Keep authentication, emails, lifecycle rules, and administration consistent across tenants.",
      "Present a more trustworthy public experience with multilingual entry points and clear next steps."
    ],
    processTitle: "How ReserveNook fits the first rollout",
    process: [
      {
        step: "01",
        title: "Register a company",
        description: "Capture business type, admin account, language, and plan in one structured onboarding flow."
      },
      {
        step: "02",
        title: "Verify and access",
        description: "Activation, password recovery, and branded email touchpoints keep access secure and clear."
      },
      {
        step: "03",
        title: "Operate confidently",
        description: "Platform admins manage inactivity rules while each company keeps its own booking surface."
      }
    ],
    faqTitle: "Booking platform questions",
    faq: [
      {
        question: "Who is ReserveNook for?",
        answer:
          "ReserveNook is designed for service businesses that need online booking for appointments, classes, or restaurant reservations."
      },
      {
        question: "Can the same platform support different booking models?",
        answer:
          "Yes. The product is structured around one shared tenant and account model with specialized booking modules per business type."
      },
      {
        question: "Does it support multilingual onboarding?",
        answer:
          "Yes. Public pages and transactional emails are localized so companies can onboard in their selected language."
      }
    ],
    secondaryCta: "See how onboarding works"
  },
  de: {
    trustLabel: "Für Dienstleistungsunternehmen mit komplexen Buchungsabläufen entwickelt",
    heroKicker: ["Termine", "Kurse", "Restaurantreservierungen"],
    metrics: [
      { label: "Gemeinsames Onboarding", value: "Ein Kontomodell" },
      { label: "Lokalisierte Oberfläche", value: "3 öffentliche Sprachen" },
      { label: "Operative Kontrolle", value: "Richtlinien für Inaktivität" }
    ],
    outcomesTitle: "Ein klareres Buchungserlebnis für jeden Geschäftstyp",
    outcomes: [
      "Starten Sie ein Firmenkonto mit dem passenden Buchungsablauf vom ersten Tag an.",
      "Halten Sie Authentifizierung, E-Mails, Lifecycle-Regeln und Administration über alle Mandanten konsistent.",
      "Bieten Sie einen vertrauenswürdigeren öffentlichen Auftritt mit mehrsprachigen Einstiegsseiten und klaren nächsten Schritten."
    ],
    processTitle: "So passt ReserveNook zum ersten Produktumfang",
    process: [
      {
        step: "01",
        title: "Firma registrieren",
        description: "Erfassen Sie Geschäftstyp, Administratorkonto, Sprache und Plan in einem strukturierten Onboarding."
      },
      {
        step: "02",
        title: "Verifizieren und anmelden",
        description: "Aktivierung, Passwortwiederherstellung und markengerechte E-Mails sorgen für einen klaren Zugriff."
      },
      {
        step: "03",
        title: "Sicher betreiben",
        description: "Plattformadministratoren verwalten Inaktivitätsregeln, während jede Firma ihre eigene Buchungsoberfläche behält."
      }
    ],
    faqTitle: "Fragen zur Buchungsplattform",
    faq: [
      {
        question: "Für wen ist ReserveNook gedacht?",
        answer:
          "ReserveNook ist für Dienstleistungsunternehmen konzipiert, die Online-Buchungen für Termine, Kurse oder Restaurantreservierungen benötigen."
      },
      {
        question: "Kann dieselbe Plattform verschiedene Buchungsmodelle abbilden?",
        answer:
          "Ja. Das Produkt basiert auf einem gemeinsamen Mandanten- und Kontomodell mit spezialisierten Buchungsmodulen je Geschäftstyp."
      },
      {
        question: "Unterstützt die Plattform mehrsprachiges Onboarding?",
        answer:
          "Ja. Öffentliche Seiten und transaktionale E-Mails werden lokalisiert ausgeliefert."
      }
    ],
    secondaryCta: "Onboarding ansehen"
  },
  pt: {
    trustLabel: "Criado para empresas de serviços com fluxos de reserva exigentes",
    heroKicker: ["Consultas", "Aulas", "Reservas de restaurante"],
    metrics: [
      { label: "Onboarding partilhado", value: "Um modelo de conta" },
      { label: "Experiência localizada", value: "3 idiomas públicos" },
      { label: "Controlo operacional", value: "Políticas de inatividade" }
    ],
    outcomesTitle: "Uma experiência de reserva mais clara para cada tipo de negócio",
    outcomes: [
      "Lance a conta da empresa com o fluxo de reserva certo desde o primeiro dia.",
      "Mantenha autenticação, emails, regras de ciclo de vida e administração consistentes entre tenants.",
      "Apresente uma experiência pública mais credível com páginas multilingues e próximos passos claros."
    ],
    processTitle: "Como a ReserveNook encaixa na primeira fase",
    process: [
      {
        step: "01",
        title: "Registar a empresa",
        description: "Recolha o tipo de negócio, conta de administrador, idioma e plano num onboarding estruturado."
      },
      {
        step: "02",
        title: "Validar e aceder",
        description: "Ativação, recuperação de palavra-passe e emails com marca tornam o acesso mais claro e seguro."
      },
      {
        step: "03",
        title: "Operar com confiança",
        description: "Os administradores da plataforma definem regras de inatividade enquanto cada empresa mantém a sua própria presença."
      }
    ],
    faqTitle: "Perguntas sobre a plataforma de reservas",
    faq: [
      {
        question: "Para quem foi criada a ReserveNook?",
        answer:
          "A ReserveNook foi pensada para empresas de serviços que precisam de reservas online para consultas, aulas ou restaurantes."
      },
      {
        question: "A mesma plataforma pode suportar modelos diferentes de reserva?",
        answer:
          "Sim. O produto assenta num modelo comum de tenant e conta, com módulos especializados por tipo de negócio."
      },
      {
        question: "Existe onboarding multilingue?",
        answer:
          "Sim. As páginas públicas e os emails transacionais são apresentados no idioma selecionado pela empresa."
      }
    ],
    secondaryCta: "Ver o onboarding"
  }
};

export function PublicHomePage({ locale }: PublicHomePageProps) {
  const messages = getPublicMessages(locale);
  const sections = localizedSections[locale];

  const faqSchema = {
    "@context": "https://schema.org",
    "@type": "FAQPage",
    mainEntity: sections.faq.map((item) => ({
      "@type": "Question",
      name: item.question,
      acceptedAnswer: {
        "@type": "Answer",
        text: item.answer
      }
    }))
  };

  const softwareSchema = {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    name: "ReserveNook",
    applicationCategory: "BusinessApplication",
    operatingSystem: "Web",
    offers: {
      "@type": "Offer",
      price: "0",
      priceCurrency: "USD"
    },
    description: messages.heroDescription
  };

  return (
    <Stack spacing={{ xs: 5, md: 8 }}>
      <Box component="script" type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(faqSchema) }} />
      <Box
        component="script"
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(softwareSchema) }}
      />

      <Grid container spacing={3} alignItems="stretch">
        <Grid size={{ xs: 12, md: 7 }}>
          <Paper
            elevation={0}
            sx={{
              height: "100%",
              border: "1px solid rgba(83, 58, 43, 0.12)",
              borderRadius: 8,
              px: { xs: 3, md: 5 },
              py: { xs: 4, md: 6 },
              background:
                "linear-gradient(135deg, rgba(255,248,240,0.98) 0%, rgba(247,236,221,0.96) 50%, rgba(242,223,196,0.92) 100%)",
              boxShadow: "0 24px 80px rgba(59, 39, 26, 0.10)"
            }}
          >
            <Stack spacing={3.5}>
              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                <Chip
                  label={messages.heroEyebrow}
                  sx={{ backgroundColor: "rgba(180, 90, 56, 0.12)", color: "primary.dark", fontWeight: 700 }}
                />
                <Chip
                  label={sections.trustLabel}
                  variant="outlined"
                  sx={{ borderColor: "rgba(83, 58, 43, 0.12)", color: "text.secondary" }}
                />
              </Stack>

              <Typography variant="h1" component="h1" sx={{ maxWidth: 760 }}>
                {messages.heroTitle}
              </Typography>

              <Typography variant="h6" color="text.secondary" sx={{ maxWidth: 700 }}>
                {messages.heroDescription}
              </Typography>

              <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
                {sections.heroKicker.map((item) => (
                  <Chip
                    key={item}
                    label={item}
                    sx={{
                      borderRadius: 999,
                      backgroundColor: "rgba(255,255,255,0.64)",
                      border: "1px solid rgba(83, 58, 43, 0.10)",
                      fontWeight: 700
                    }}
                  />
                ))}
              </Stack>

              <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
                <Button component={Link} href={`/${locale}/register`} variant="contained" size="large">
                  {messages.heroPrimaryCta}
                </Button>
                <Button component={Link} href="#process" variant="outlined" size="large">
                  {sections.secondaryCta}
                </Button>
              </Stack>
            </Stack>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 5 }}>
          <Stack spacing={2} sx={{ height: "100%" }}>
            {sections.metrics.map((metric) => (
              <Paper
                key={metric.label}
                elevation={0}
                sx={{
                  flex: 1,
                  p: 3,
                  borderRadius: 6,
                  border: "1px solid rgba(83, 58, 43, 0.12)",
                  backgroundColor: "rgba(255, 251, 247, 0.88)",
                  boxShadow: "0 18px 50px rgba(59, 39, 26, 0.06)"
                }}
              >
                <Typography color="text.secondary" sx={{ mb: 1 }}>
                  {metric.label}
                </Typography>
                <Typography variant="h4">{metric.value}</Typography>
              </Paper>
            ))}
          </Stack>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper
            elevation={0}
            sx={{
              p: { xs: 3, md: 4 },
              borderRadius: 6,
              border: "1px solid rgba(83, 58, 43, 0.12)",
              backgroundColor: "rgba(255, 252, 247, 0.86)"
            }}
          >
            <Stack spacing={2.5}>
              <Typography variant="h3" component="h2">
                {sections.outcomesTitle}
              </Typography>
              {sections.outcomes.map((item) => (
                <Stack key={item} direction="row" spacing={1.5} alignItems="flex-start">
                  <Box
                    sx={{
                      width: 12,
                      height: 12,
                      borderRadius: "50%",
                      mt: 1,
                      backgroundColor: "primary.main",
                      flexShrink: 0
                    }}
                  />
                  <Typography color="text.secondary">{item}</Typography>
                </Stack>
              ))}
            </Stack>
          </Paper>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Paper
            id="scope"
            elevation={0}
            sx={{
              p: { xs: 3, md: 4 },
              borderRadius: 6,
              border: "1px solid rgba(83, 58, 43, 0.12)",
              backgroundColor: "rgba(255, 252, 247, 0.86)"
            }}
          >
            <Stack spacing={2.5}>
              <Typography variant="h3" component="h2">
                {messages.spotlightTitle}
              </Typography>
              {messages.spotlightItems.map((item, index) => (
                <Box key={item}>
                  <Typography variant="body2" color="text.secondary">
                    0{index + 1}
                  </Typography>
                  <Typography variant="h6" sx={{ mt: 0.5 }}>
                    {item}
                  </Typography>
                  {index < messages.spotlightItems.length - 1 ? <Divider sx={{ mt: 2.5 }} /> : null}
                </Box>
              ))}
            </Stack>
          </Paper>
        </Grid>
      </Grid>

      <Stack id="process" spacing={3}>
        <Typography variant="h3" component="h2">
          {sections.processTitle}
        </Typography>
        <Grid container spacing={3}>
          {sections.process.map((item) => (
            <Grid key={item.step} size={{ xs: 12, md: 4 }}>
              <Paper
                elevation={0}
                sx={{
                  height: "100%",
                  p: 3,
                  borderRadius: 6,
                  border: "1px solid rgba(83, 58, 43, 0.12)",
                  backgroundColor: "rgba(255, 252, 247, 0.86)"
                }}
              >
                <Typography variant="overline" sx={{ color: "primary.main", letterSpacing: "0.14em" }}>
                  {item.step}
                </Typography>
                <Typography variant="h5" sx={{ mt: 1, mb: 1.5 }}>
                  {item.title}
                </Typography>
                <Typography color="text.secondary">{item.description}</Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Stack>

      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 6,
          border: "1px solid rgba(83, 58, 43, 0.12)",
          backgroundColor: "rgba(255, 252, 247, 0.86)"
        }}
      >
        <Stack spacing={3}>
          <Typography variant="h3" component="h2">
            {sections.faqTitle}
          </Typography>
          <Grid container spacing={2.5}>
            {sections.faq.map((item) => (
              <Grid key={item.question} size={{ xs: 12, md: 4 }}>
                <Stack spacing={1}>
                  <Typography variant="h6">{item.question}</Typography>
                  <Typography color="text.secondary">{item.answer}</Typography>
                </Stack>
              </Grid>
            ))}
          </Grid>
        </Stack>
      </Paper>
    </Stack>
  );
}
