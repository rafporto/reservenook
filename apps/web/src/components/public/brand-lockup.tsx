"use client";

import Image from "next/image";
import Link from "next/link";
import { Box } from "@mui/material";
import type { SupportedLocale } from "@/lib/i18n/locales";

type BrandLockupProps = {
  locale: SupportedLocale;
  width?: number;
  height?: number;
};

export function BrandLockup({ locale, width = 220, height = 44 }: BrandLockupProps) {
  return (
    <Box component={Link} href={`/${locale}`} sx={{ display: "inline-flex", alignItems: "center" }}>
      <Image
        src="/reservenook-logo.svg"
        alt="ReserveNook"
        width={width}
        height={height}
        priority
        style={{ width: "auto", height: `${height}px` }}
      />
    </Box>
  );
}
