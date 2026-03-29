import { PublicHomePage } from "@/features/public/home/public-home-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type LocaleHomePageProps = {
  params: Promise<{ locale: string }>;
};

export default async function LocaleHomePage({ params }: LocaleHomePageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicHomePage locale={locale} />;
}
