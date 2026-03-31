import type { Metadata } from "next";
import { PublicForgotPasswordPage } from "@/features/public/auth/public-forgot-password-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getForgotPasswordMetadata } from "@/lib/seo/public-seo";

type ForgotPasswordPageProps = {
  params: Promise<{ locale: string }>;
};

export async function generateMetadata({ params }: ForgotPasswordPageProps): Promise<Metadata> {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return getForgotPasswordMetadata(locale);
}

export default async function ForgotPasswordPage({ params }: ForgotPasswordPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicForgotPasswordPage locale={locale} />;
}
