import type { Metadata } from "next";
import { PublicResendActivationPage } from "@/features/public/activation/public-resend-activation-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getResendActivationMetadata } from "@/lib/seo/public-seo";

type ResendActivationPageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ email?: string }>;
};

export async function generateMetadata({ params }: ResendActivationPageProps): Promise<Metadata> {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return getResendActivationMetadata(locale);
}

export default async function ResendActivationPage({ params, searchParams }: ResendActivationPageProps) {
  const { locale: rawLocale } = await params;
  const { email } = await searchParams;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicResendActivationPage locale={locale} initialEmail={email} />;
}
