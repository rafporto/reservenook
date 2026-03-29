import { PublicResendActivationPage } from "@/features/public/activation/public-resend-activation-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type ResendActivationPageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ email?: string }>;
};

export default async function ResendActivationPage({ params, searchParams }: ResendActivationPageProps) {
  const { locale: rawLocale } = await params;
  const { email } = await searchParams;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicResendActivationPage locale={locale} initialEmail={email} />;
}
