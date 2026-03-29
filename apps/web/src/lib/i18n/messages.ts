import type { SupportedLocale } from "@/lib/i18n/locales";

type PublicMessages = {
  localeLabel: string;
  navProduct: string;
  navRegister: string;
  navLogin: string;
  heroEyebrow: string;
  heroTitle: string;
  heroDescription: string;
  heroPrimaryCta: string;
  heroSecondaryCta: string;
  spotlightTitle: string;
  spotlightItems: string[];
  registrationTitle: string;
  registrationDescription: string;
  activationTitle: string;
  activationLoading: string;
  activationSuccess: string;
  activationAlreadyActive: string;
  activationExpired: string;
  activationInvalid: string;
  activationNextStep: string;
  activationResendCta: string;
  resendActivationTitle: string;
  resendActivationDescription: string;
  resendActivationEmailLabel: string;
  resendActivationSubmit: string;
  resendActivationSubmitting: string;
  resendActivationNeutralSuccess: string;
  loginTitle: string;
  loginDescription: string;
  loginEmailLabel: string;
  loginPasswordLabel: string;
  loginSubmit: string;
  loginSubmitting: string;
  loginInvalidCredentials: string;
  loginActivationRequired: string;
  loginInactiveCompany: string;
  loginResendActivationCta: string;
  loginNextStepCompany: string;
  loginNextStepPlatform: string;
  formCompanyName: string;
  formBusinessType: string;
  formSlug: string;
  formEmail: string;
  formPassword: string;
  formPlanType: string;
  formDefaultLanguage: string;
  formDefaultLocale: string;
  submitLabel: string;
  submittingLabel: string;
  successMessage: string;
  genericError: string;
  businessTypeAppointment: string;
  businessTypeClass: string;
  businessTypeRestaurant: string;
  planTrial: string;
  planPaid: string;
  requiredField: string;
  invalidEmail: string;
  invalidSlug: string;
  passwordTooShort: string;
};

const messages: Record<SupportedLocale, PublicMessages> = {
  en: {
    localeLabel: "Language",
    navProduct: "Product",
    navRegister: "Register company",
    navLogin: "Login",
    heroEyebrow: "Unified booking platform",
    heroTitle: "Appointments, classes, and restaurant reservations in one product.",
    heroDescription:
      "Reservenook gives each company the right booking flow without splitting the platform into separate products.",
    heroPrimaryCta: "Start company registration",
    heroSecondaryCta: "Review platform scope",
    spotlightTitle: "Built for the first product slice",
    spotlightItems: [
      "One tenant model for all companies",
      "Shared registration, auth, notifications, and subscriptions",
      "Specialized booking modules for appointments, classes, and restaurants"
    ],
    registrationTitle: "Register your company",
    registrationDescription:
      "Create the company account and initial company admin. The company stays pending until the activation email is confirmed.",
    activationTitle: "Account activation",
    activationLoading: "Validating your activation link...",
    activationSuccess: "Your company account is active. The initial admin account is now verified.",
    activationAlreadyActive: "This company account is already active.",
    activationExpired: "This activation link has expired. Request a new activation email.",
    activationInvalid: "This activation link is invalid.",
    activationNextStep: "Your account state is ready. Continue to login to enter the platform.",
    activationResendCta: "Request a new activation email",
    resendActivationTitle: "Resend activation email",
    resendActivationDescription:
      "Enter the registration email. If the account is still pending activation, a new activation email will be sent.",
    resendActivationEmailLabel: "Registration email",
    resendActivationSubmit: "Send activation email",
    resendActivationSubmitting: "Sending activation email...",
    resendActivationNeutralSuccess:
      "If the account is pending activation, a new activation email will be sent.",
    loginTitle: "Login",
    loginDescription: "Sign in with the company admin or platform admin account.",
    loginEmailLabel: "Email",
    loginPasswordLabel: "Password",
    loginSubmit: "Login",
    loginSubmitting: "Logging in...",
    loginInvalidCredentials: "Invalid email or password.",
    loginActivationRequired: "Your account is not active yet. Request a new activation email.",
    loginInactiveCompany: "The company account is not active.",
    loginResendActivationCta: "Resend activation email",
    loginNextStepCompany: "You will be redirected to the company backoffice.",
    loginNextStepPlatform: "You will be redirected to the platform admin area.",
    formCompanyName: "Company name",
    formBusinessType: "Business type",
    formSlug: "Public slug",
    formEmail: "Admin email",
    formPassword: "Password",
    formPlanType: "Plan",
    formDefaultLanguage: "Default language",
    formDefaultLocale: "Default locale",
    submitLabel: "Create company account",
    submittingLabel: "Creating company account...",
    successMessage: "Registration received. Check your email to activate your account.",
    genericError: "Registration could not be completed. Please review the form and try again.",
    businessTypeAppointment: "Appointments",
    businessTypeClass: "Classes",
    businessTypeRestaurant: "Restaurant reservations",
    planTrial: "Trial",
    planPaid: "Paid annual plan",
    requiredField: "This field is required.",
    invalidEmail: "Enter a valid email address.",
    invalidSlug: "Use lowercase letters, numbers, and hyphens only.",
    passwordTooShort: "Password must contain at least 8 characters."
  },
  de: {
    localeLabel: "Sprache",
    navProduct: "Produkt",
    navRegister: "Firma registrieren",
    navLogin: "Anmelden",
    heroEyebrow: "Einheitliche Buchungsplattform",
    heroTitle: "Termine, Kurse und Restaurantreservierungen in einem Produkt.",
    heroDescription:
      "Reservenook bietet jedem Unternehmen den passenden Buchungsablauf, ohne die Plattform in mehrere Produkte aufzuteilen.",
    heroPrimaryCta: "Firmenregistrierung starten",
    heroSecondaryCta: "Plattformumfang ansehen",
    spotlightTitle: "Für den ersten Produktschnitt ausgelegt",
    spotlightItems: [
      "Ein Mandantenmodell für alle Firmen",
      "Gemeinsame Registrierung, Authentifizierung, Benachrichtigungen und Abos",
      "Spezialisierte Buchungsmodule für Termine, Kurse und Restaurants"
    ],
    registrationTitle: "Firma registrieren",
    registrationDescription:
      "Erstellen Sie das Firmenkonto und den ersten Firmenadministrator. Die Firma bleibt ausstehend, bis die Aktivierungs-E-Mail bestätigt wurde.",
    activationTitle: "Kontoaktivierung",
    activationLoading: "Ihr Aktivierungslink wird geprüft...",
    activationSuccess: "Ihr Firmenkonto ist aktiv. Das erste Administratorkonto ist jetzt verifiziert.",
    activationAlreadyActive: "Dieses Firmenkonto ist bereits aktiv.",
    activationExpired: "Dieser Aktivierungslink ist abgelaufen. Fordern Sie eine neue Aktivierungs-E-Mail an.",
    activationInvalid: "Dieser Aktivierungslink ist ungültig.",
    activationNextStep: "Ihr Kontostatus ist bereit. Fahren Sie mit der Anmeldung fort, um die Plattform zu betreten.",
    activationResendCta: "Neue Aktivierungs-E-Mail anfordern",
    resendActivationTitle: "Aktivierungs-E-Mail erneut senden",
    resendActivationDescription:
      "Geben Sie die Registrierungs-E-Mail ein. Wenn das Konto noch auf Aktivierung wartet, wird eine neue Aktivierungs-E-Mail gesendet.",
    resendActivationEmailLabel: "Registrierungs-E-Mail",
    resendActivationSubmit: "Aktivierungs-E-Mail senden",
    resendActivationSubmitting: "Aktivierungs-E-Mail wird gesendet...",
    resendActivationNeutralSuccess:
      "Wenn das Konto noch auf Aktivierung wartet, wird eine neue Aktivierungs-E-Mail gesendet.",
    loginTitle: "Anmelden",
    loginDescription: "Melden Sie sich mit dem Firmenadministrator- oder Plattformadministratorkonto an.",
    loginEmailLabel: "E-Mail",
    loginPasswordLabel: "Passwort",
    loginSubmit: "Anmelden",
    loginSubmitting: "Anmeldung läuft...",
    loginInvalidCredentials: "Ungültige E-Mail oder ungültiges Passwort.",
    loginActivationRequired: "Ihr Konto ist noch nicht aktiv. Fordern Sie eine neue Aktivierungs-E-Mail an.",
    loginInactiveCompany: "Das Firmenkonto ist nicht aktiv.",
    loginResendActivationCta: "Aktivierungs-E-Mail erneut senden",
    loginNextStepCompany: "Sie werden in das Firmen-Backoffice weitergeleitet.",
    loginNextStepPlatform: "Sie werden in den Plattform-Adminbereich weitergeleitet.",
    formCompanyName: "Firmenname",
    formBusinessType: "Geschäftstyp",
    formSlug: "Öffentlicher Slug",
    formEmail: "Administrator-E-Mail",
    formPassword: "Passwort",
    formPlanType: "Plan",
    formDefaultLanguage: "Standardsprache",
    formDefaultLocale: "Standard-Locale",
    submitLabel: "Firmenkonto erstellen",
    submittingLabel: "Firmenkonto wird erstellt...",
    successMessage: "Registrierung erhalten. Prüfen Sie Ihre E-Mails, um Ihr Konto zu aktivieren.",
    genericError: "Die Registrierung konnte nicht abgeschlossen werden. Bitte prüfen Sie das Formular und versuchen Sie es erneut.",
    businessTypeAppointment: "Termine",
    businessTypeClass: "Kurse",
    businessTypeRestaurant: "Restaurantreservierungen",
    planTrial: "Testphase",
    planPaid: "Bezahlter Jahresplan",
    requiredField: "Dieses Feld ist erforderlich.",
    invalidEmail: "Geben Sie eine gültige E-Mail-Adresse ein.",
    invalidSlug: "Verwenden Sie nur Kleinbuchstaben, Zahlen und Bindestriche.",
    passwordTooShort: "Das Passwort muss mindestens 8 Zeichen enthalten."
  },
  pt: {
    localeLabel: "Idioma",
    navProduct: "Produto",
    navRegister: "Registar empresa",
    navLogin: "Iniciar sessão",
    heroEyebrow: "Plataforma unificada de reservas",
    heroTitle: "Consultas, aulas e reservas de restaurante num único produto.",
    heroDescription:
      "A Reservenook dá a cada empresa o fluxo de reserva certo sem dividir a plataforma em produtos separados.",
    heroPrimaryCta: "Iniciar registo da empresa",
    heroSecondaryCta: "Ver âmbito da plataforma",
    spotlightTitle: "Preparado para a primeira fase do produto",
    spotlightItems: [
      "Um único modelo de tenant para todas as empresas",
      "Registo, autenticação, notificações e subscrições partilhados",
      "Módulos especializados para consultas, aulas e restaurantes"
    ],
    registrationTitle: "Registar a sua empresa",
    registrationDescription:
      "Crie a conta da empresa e o administrador inicial. A empresa permanece pendente até a confirmação do email de ativação.",
    activationTitle: "Ativação da conta",
    activationLoading: "A validar o seu link de ativação...",
    activationSuccess: "A conta da empresa está ativa. O administrador inicial já está verificado.",
    activationAlreadyActive: "Esta conta da empresa já está ativa.",
    activationExpired: "Este link de ativação expirou. Peça um novo email de ativação.",
    activationInvalid: "Este link de ativação é inválido.",
    activationNextStep: "O estado da sua conta já está preparado. Continue para o login para entrar na plataforma.",
    activationResendCta: "Pedir novo email de ativação",
    resendActivationTitle: "Reenviar email de ativação",
    resendActivationDescription:
      "Introduza o email de registo. Se a conta ainda estiver pendente, será enviado um novo email de ativação.",
    resendActivationEmailLabel: "Email de registo",
    resendActivationSubmit: "Enviar email de ativação",
    resendActivationSubmitting: "A enviar email de ativação...",
    resendActivationNeutralSuccess:
      "Se a conta ainda estiver pendente, será enviado um novo email de ativação.",
    loginTitle: "Iniciar sessão",
    loginDescription: "Entre com a conta de administrador da empresa ou de administrador da plataforma.",
    loginEmailLabel: "Email",
    loginPasswordLabel: "Palavra-passe",
    loginSubmit: "Iniciar sessão",
    loginSubmitting: "A iniciar sessão...",
    loginInvalidCredentials: "Email ou palavra-passe inválidos.",
    loginActivationRequired: "A sua conta ainda não está ativa. Peça um novo email de ativação.",
    loginInactiveCompany: "A conta da empresa não está ativa.",
    loginResendActivationCta: "Reenviar email de ativação",
    loginNextStepCompany: "Será redirecionado para o backoffice da empresa.",
    loginNextStepPlatform: "Será redirecionado para a área de administração da plataforma.",
    formCompanyName: "Nome da empresa",
    formBusinessType: "Tipo de negócio",
    formSlug: "Slug público",
    formEmail: "Email do administrador",
    formPassword: "Palavra-passe",
    formPlanType: "Plano",
    formDefaultLanguage: "Idioma predefinido",
    formDefaultLocale: "Locale predefinido",
    submitLabel: "Criar conta da empresa",
    submittingLabel: "A criar conta da empresa...",
    successMessage: "Registo recebido. Verifique o seu email para ativar a conta.",
    genericError: "Não foi possível concluir o registo. Reveja o formulário e tente novamente.",
    businessTypeAppointment: "Consultas",
    businessTypeClass: "Aulas",
    businessTypeRestaurant: "Reservas de restaurante",
    planTrial: "Período experimental",
    planPaid: "Plano anual pago",
    requiredField: "Este campo é obrigatório.",
    invalidEmail: "Introduza um endereço de email válido.",
    invalidSlug: "Use apenas letras minúsculas, números e hífens.",
    passwordTooShort: "A palavra-passe deve ter pelo menos 8 caracteres."
  }
};

export function getPublicMessages(locale: SupportedLocale): PublicMessages {
  return messages[locale];
}
