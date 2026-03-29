import { PublicLoginPage } from "@/features/public/auth/public-login-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type LoginPageProps = {
  params: Promise<{ locale: string }>;
};

export default async function LoginPage({ params }: LoginPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicLoginPage locale={locale} />;
}
