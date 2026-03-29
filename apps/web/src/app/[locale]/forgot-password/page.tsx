import { PublicForgotPasswordPage } from "@/features/public/auth/public-forgot-password-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type ForgotPasswordPageProps = {
  params: Promise<{ locale: string }>;
};

export default async function ForgotPasswordPage({ params }: ForgotPasswordPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicForgotPasswordPage locale={locale} />;
}
