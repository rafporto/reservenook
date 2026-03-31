import type { Metadata } from "next";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { supportedLocales } from "@/lib/i18n/locales";

type LocaleSeoCopy = {
  siteName: string;
  homeTitle: string;
  homeDescription: string;
  registerTitle: string;
  registerDescription: string;
  loginTitle: string;
  loginDescription: string;
  forgotPasswordTitle: string;
  forgotPasswordDescription: string;
  resendActivationTitle: string;
  resendActivationDescription: string;
  activationTitle: string;
  activationDescription: string;
  resetPasswordTitle: string;
  resetPasswordDescription: string;
};

const seoCopy: Record<SupportedLocale, LocaleSeoCopy> = {
  en: {
    siteName: "ReserveNook",
    homeTitle: "Booking platform for appointments, classes, and restaurant reservations",
    homeDescription:
      "ReserveNook helps service businesses manage appointments, class schedules, and restaurant reservations from one multilingual booking platform.",
    registerTitle: "Register your company",
    registerDescription:
      "Create your ReserveNook company account, set your business type, and start onboarding your team on a unified booking platform.",
    loginTitle: "Company and platform admin login",
    loginDescription:
      "Secure login for company admins and platform administrators using the ReserveNook booking platform.",
    forgotPasswordTitle: "Recover your account password",
    forgotPasswordDescription:
      "Request a secure password reset for your ReserveNook company or platform admin account.",
    resendActivationTitle: "Resend company activation email",
    resendActivationDescription:
      "Request a new ReserveNook activation email for a company account that is still pending verification.",
    activationTitle: "Activate your company account",
    activationDescription:
      "Verify your ReserveNook company account and confirm the initial admin access.",
    resetPasswordTitle: "Reset your password",
    resetPasswordDescription:
      "Set a new password to restore access to your ReserveNook account."
  },
  de: {
    siteName: "ReserveNook",
    homeTitle: "Buchungsplattform für Termine, Kurse und Restaurantreservierungen",
    homeDescription:
      "ReserveNook unterstützt Dienstleistungsunternehmen bei der Verwaltung von Terminen, Kursplänen und Restaurantreservierungen auf einer mehrsprachigen Plattform.",
    registerTitle: "Firma registrieren",
    registerDescription:
      "Erstellen Sie Ihr ReserveNook-Firmenkonto, wählen Sie Ihren Geschäftstyp und starten Sie das Onboarding auf einer einheitlichen Buchungsplattform.",
    loginTitle: "Login für Firmen- und Plattformadministratoren",
    loginDescription:
      "Sicherer Login für Firmenadministratoren und Plattformadministratoren in ReserveNook.",
    forgotPasswordTitle: "Passwort wiederherstellen",
    forgotPasswordDescription:
      "Fordern Sie ein sicheres Zurücksetzen des Passworts für Ihr ReserveNook-Konto an.",
    resendActivationTitle: "Aktivierungs-E-Mail erneut senden",
    resendActivationDescription:
      "Fordern Sie eine neue Aktivierungs-E-Mail für ein noch nicht verifiziertes Firmenkonto an.",
    activationTitle: "Firmenkonto aktivieren",
    activationDescription:
      "Bestätigen Sie Ihr ReserveNook-Firmenkonto und verifizieren Sie den ersten Administratorzugang.",
    resetPasswordTitle: "Passwort zurücksetzen",
    resetPasswordDescription:
      "Legen Sie ein neues Passwort fest, um wieder auf Ihr ReserveNook-Konto zuzugreifen."
  },
  pt: {
    siteName: "ReserveNook",
    homeTitle: "Plataforma de reservas para consultas, aulas e restaurantes",
    homeDescription:
      "A ReserveNook ajuda empresas de serviços a gerir consultas, horários de aulas e reservas de restaurante numa plataforma multilingue.",
    registerTitle: "Registar empresa",
    registerDescription:
      "Crie a conta da sua empresa na ReserveNook, escolha o tipo de negócio e inicie a configuração numa plataforma unificada de reservas.",
    loginTitle: "Login para administradores da empresa e da plataforma",
    loginDescription:
      "Login seguro para administradores de empresa e administradores da plataforma na ReserveNook.",
    forgotPasswordTitle: "Recuperar palavra-passe",
    forgotPasswordDescription:
      "Peça uma redefinição segura da palavra-passe da sua conta ReserveNook.",
    resendActivationTitle: "Reenviar email de ativação",
    resendActivationDescription:
      "Peça um novo email de ativação para uma conta de empresa ainda pendente de verificação.",
    activationTitle: "Ativar conta da empresa",
    activationDescription:
      "Confirme a conta da sua empresa na ReserveNook e valide o acesso do administrador inicial.",
    resetPasswordTitle: "Redefinir palavra-passe",
    resetPasswordDescription:
      "Defina uma nova palavra-passe para recuperar o acesso à sua conta ReserveNook."
  }
};

function getSiteUrl() {
  return process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000";
}

function getLanguageAlternates(pathname = "") {
  return Object.fromEntries(supportedLocales.map((locale) => [locale, `${getSiteUrl()}/${locale}${pathname}`]));
}

function buildMetadata({
  locale,
  title,
  description,
  pathname = "",
  noIndex = false
}: {
  locale: SupportedLocale;
  title: string;
  description: string;
  pathname?: string;
  noIndex?: boolean;
}): Metadata {
  const url = `${getSiteUrl()}/${locale}${pathname}`;

  return {
    title,
    description,
    alternates: {
      canonical: url,
      languages: getLanguageAlternates(pathname)
    },
    openGraph: {
      title,
      description,
      url,
      siteName: seoCopy[locale].siteName,
      locale,
      type: "website"
    },
    twitter: {
      card: "summary_large_image",
      title,
      description
    },
    robots: noIndex
      ? {
          index: false,
          follow: false,
          googleBot: {
            index: false,
            follow: false
          }
        }
      : undefined
  };
}

export function getPublicHomeMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.homeTitle,
    description: copy.homeDescription
  });
}

export function getRegisterMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.registerTitle,
    description: copy.registerDescription,
    pathname: "/register"
  });
}

export function getLoginMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.loginTitle,
    description: copy.loginDescription,
    pathname: "/login"
  });
}

export function getForgotPasswordMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.forgotPasswordTitle,
    description: copy.forgotPasswordDescription,
    pathname: "/forgot-password",
    noIndex: true
  });
}

export function getResendActivationMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.resendActivationTitle,
    description: copy.resendActivationDescription,
    pathname: "/resend-activation",
    noIndex: true
  });
}

export function getActivationMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.activationTitle,
    description: copy.activationDescription,
    pathname: "/activate",
    noIndex: true
  });
}

export function getResetPasswordMetadata(locale: SupportedLocale): Metadata {
  const copy = seoCopy[locale];

  return buildMetadata({
    locale,
    title: copy.resetPasswordTitle,
    description: copy.resetPasswordDescription,
    pathname: "/reset-password",
    noIndex: true
  });
}

export function getSiteMetadata(): Metadata {
  return {
    metadataBase: new URL(getSiteUrl()),
    title: {
      default: "ReserveNook",
      template: "%s | ReserveNook"
    },
    description:
      "ReserveNook is a multilingual booking platform for appointments, classes, and restaurant reservations.",
    applicationName: "ReserveNook",
    keywords: [
      "booking platform",
      "appointment scheduling",
      "class bookings",
      "restaurant reservations",
      "multilingual booking software",
      "tenant booking platform"
    ],
    category: "business",
    icons: {
      icon: "/icon.svg",
      shortcut: "/icon.svg",
      apple: "/icon.svg"
    },
    openGraph: {
      siteName: "ReserveNook",
      type: "website"
    },
    twitter: {
      card: "summary_large_image"
    }
  };
}
