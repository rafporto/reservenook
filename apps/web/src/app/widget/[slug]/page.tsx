import { Suspense } from "react";
import { EmbeddedWidgetScreen } from "@/features/public/widget/embedded-widget-screen";

type Props = {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ locale?: string; token?: string; theme?: string }>;
};

export const metadata = {
  robots: {
    index: false,
    follow: false
  }
};

export default async function WidgetPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const { locale, token, theme } = await searchParams;

  return (
    <Suspense fallback={null}>
      <EmbeddedWidgetScreen slug={slug} locale={(locale as "en" | "de" | "pt" | undefined) ?? "en"} token={token ?? ""} theme={theme ?? "minimal"} />
    </Suspense>
  );
}
