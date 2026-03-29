import { PublicShell } from "@/components/public/public-shell";
import { getPublicMessages } from "@/lib/i18n/messages";
import { requireSupportedLocale } from "@/lib/i18n/locales";

type LocaleLayoutProps = {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
};

export default async function LocaleLayout({ children, params }: LocaleLayoutProps) {
  const { locale: rawLocale } = await params;
  const locale = requireSupportedLocale(rawLocale);
  const messages = getPublicMessages(locale);

  return (
    <PublicShell
      locale={locale}
      navigation={{
        localeLabel: messages.localeLabel,
        product: messages.navProduct,
        register: messages.navRegister,
        login: messages.navLogin
      }}
    >
      {children}
    </PublicShell>
  );
}
