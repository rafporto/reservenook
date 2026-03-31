import type { Metadata } from "next";
import { PublicRegistrationPage } from "@/features/public/registration/public-registration-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getRegisterMetadata } from "@/lib/seo/public-seo";

type RegisterPageProps = {
  params: Promise<{ locale: string }>;
};

export async function generateMetadata({ params }: RegisterPageProps): Promise<Metadata> {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return getRegisterMetadata(locale);
}

export default async function RegisterPage({ params }: RegisterPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicRegistrationPage locale={locale} />;
}
