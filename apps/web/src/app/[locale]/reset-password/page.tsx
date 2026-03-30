import { PublicResetPasswordPage } from "@/features/public/auth/public-reset-password-page";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type ResetPasswordPageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ token?: string }>;
};

export default async function ResetPasswordPage({ params, searchParams }: ResetPasswordPageProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);
  const { token } = await searchParams;

  return <PublicResetPasswordPage locale={locale} token={token} />;
}
