import type { Metadata } from "next";
import { PublicLoginPage } from "@/features/public/auth/public-login-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getLoginMetadata } from "@/lib/seo/public-seo";

type LoginPageProps = {
  params: Promise<{ locale: string }>;
};

export async function generateMetadata({ params }: LoginPageProps): Promise<Metadata> {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return getLoginMetadata(locale);
}

export default async function LoginPage({ params }: LoginPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicLoginPage locale={locale} />;
}
