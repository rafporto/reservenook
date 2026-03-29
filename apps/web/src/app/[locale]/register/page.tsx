import { PublicRegistrationPage } from "@/features/public/registration/public-registration-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type RegisterPageProps = {
  params: Promise<{ locale: string }>;
};

export default async function RegisterPage({ params }: RegisterPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicRegistrationPage locale={locale} />;
}
