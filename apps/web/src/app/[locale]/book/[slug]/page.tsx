import type { Metadata } from "next";
import { PublicBookingPage } from "@/features/public/booking/public-booking-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type PublicBookingRouteProps = {
  params: Promise<{ locale: string; slug: string }>;
};

export async function generateMetadata({ params }: PublicBookingRouteProps): Promise<Metadata> {
  const { locale: rawLocale, slug } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return {
    title: `ReserveNook booking | ${slug}`,
    description:
      locale === "de"
        ? "Senden Sie eine Buchungsanfrage an das Unternehmen."
        : locale === "pt"
          ? "Envie um pedido de reserva para a empresa."
          : "Send a booking request to the company."
  };
}

export default async function PublicBookingRoute({ params }: PublicBookingRouteProps) {
  const { locale: rawLocale, slug } = await params;
  const locale = requireSupportedLocale(rawLocale);

  return <PublicBookingPage locale={locale} slug={slug} />;
}
