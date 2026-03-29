import { PublicResendActivationPage } from "@/features/public/activation/public-resend-activation-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type ResendActivationPageProps = {
  params: Promise<{ locale: string }>;
};

export default async function ResendActivationPage({ params }: ResendActivationPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicResendActivationPage locale={locale} />;
}
