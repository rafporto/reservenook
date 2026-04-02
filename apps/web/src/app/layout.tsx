import type { Metadata } from "next";
import { cookies } from "next/headers";
import { Fraunces, Manrope } from "next/font/google";
import "./globals.css";
import { AppProviders } from "@/components/providers/app-providers";
import { getSiteMetadata } from "@/lib/seo/public-seo";

const displayFont = Fraunces({
  subsets: ["latin"],
  variable: "--font-display",
  weight: ["600", "700"]
});

const bodyFont = Manrope({
  subsets: ["latin"],
  variable: "--font-body",
  weight: ["400", "500", "600", "700"]
});

export const metadata: Metadata = getSiteMetadata();

export default async function RootLayout({
  children
}: Readonly<{
  children: React.ReactNode;
}>) {
  const cookieStore = await cookies();
  const locale = cookieStore.get("rn-locale")?.value ?? "en";

  return (
    <html lang={locale}>
      <body className={`${displayFont.variable} ${bodyFont.variable}`}>
        <AppProviders>{children}</AppProviders>
      </body>
    </html>
  );
}
