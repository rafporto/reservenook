import type { Metadata } from "next";
import { Stack, Typography } from "@mui/material";
import { PublicAuthFrame } from "@/components/public/public-auth-frame";
import { ActivationStatus } from "@/features/public/activation/activation-status";
import { requireSupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";
import { getActivationMetadata } from "@/lib/seo/public-seo";

type ActivatePageProps = {
  params: Promise<{ locale: string }>;
  searchParams: Promise<{ token?: string }>;
};

export async function generateMetadata({ params }: ActivatePageProps): Promise<Metadata> {
  const { locale } = await params;
  const safeLocale = requireSupportedLocale(locale);

  return getActivationMetadata(safeLocale);
}

export default async function ActivatePage({ params, searchParams }: ActivatePageProps) {
  const { locale } = await params;
  const safeLocale = requireSupportedLocale(locale);
  const { token } = await searchParams;
  const messages = getPublicMessages(safeLocale);

  return (
    <PublicAuthFrame
      locale={safeLocale}
      eyebrow={messages.activationTitle}
      title={messages.activationTitle}
      description={messages.activationNextStep}
      highlights={["Account verification", "Company activation", "Secure onboarding"]}
    >
      <Stack spacing={3}>
        <Typography variant="h5">Confirm the activation link to unlock the initial company admin account</Typography>
        <ActivationStatus locale={safeLocale} token={token} />
      </Stack>
    </PublicAuthFrame>
  );
}
