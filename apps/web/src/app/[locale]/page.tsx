import type { Metadata } from "next";
import { PublicHomePage } from "@/features/public/home/public-home-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getPublicHomeMetadata } from "@/lib/seo/public-seo";

type LocaleHomePageProps = {
  params: Promise<{ locale: string }>;
};

export async function generateMetadata({ params }: LocaleHomePageProps): Promise<Metadata> {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return getPublicHomeMetadata(locale);
}

export default async function LocaleHomePage({ params }: LocaleHomePageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicHomePage locale={locale} />;
}
