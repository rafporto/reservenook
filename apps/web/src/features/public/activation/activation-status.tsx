"use client";

import Link from "next/link";
import { Alert, Button, CircularProgress, Stack, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type ActivationApiStatus = "ACTIVATED" | "ALREADY_ACTIVE" | "EXPIRED" | "INVALID";

type ActivationStatusProps = {
  locale: SupportedLocale;
  token?: string;
};

type ActivationViewState =
  | { phase: "loading" }
  | { phase: "resolved"; status: ActivationApiStatus };

export function ActivationStatus({ locale, token }: ActivationStatusProps) {
  const messages = getPublicMessages(locale);
  const [state, setState] = useState<ActivationViewState>(
    token ? { phase: "loading" } : { phase: "resolved", status: "INVALID" }
  );

  useEffect(() => {
    if (!token) {
      return;
    }

    let active = true;

    async function confirmActivation() {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/activation/confirm`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json"
            },
            body: JSON.stringify({ token })
          }
        );

        const payload = (await response.json().catch(() => null)) as { status?: ActivationApiStatus } | null;
        const nextStatus = payload?.status ?? "INVALID";

        if (active) {
          setState({ phase: "resolved", status: nextStatus });
        }
      } catch {
        if (active) {
          setState({ phase: "resolved", status: "INVALID" });
        }
      }
    }

    confirmActivation();

    return () => {
      active = false;
    };
  }, [token]);

  if (state.phase === "loading") {
    return (
      <Stack spacing={2} alignItems="center" sx={{ py: 2 }}>
        <CircularProgress />
        <Typography color="text.secondary">{messages.activationLoading}</Typography>
      </Stack>
    );
  }

  const alertConfig = (() => {
    switch (state.status) {
      case "ACTIVATED":
        return { severity: "success" as const, message: messages.activationSuccess };
      case "ALREADY_ACTIVE":
        return { severity: "info" as const, message: messages.activationAlreadyActive };
      case "EXPIRED":
        return { severity: "warning" as const, message: messages.activationExpired };
      case "INVALID":
      default:
        return { severity: "error" as const, message: messages.activationInvalid };
    }
  })();

  return (
    <Stack spacing={2}>
      <Alert severity={alertConfig.severity}>{alertConfig.message}</Alert>
      {(state.status === "ACTIVATED" || state.status === "ALREADY_ACTIVE") ? (
        <Typography color="text.secondary">{messages.activationNextStep}</Typography>
      ) : null}
      {(state.status === "EXPIRED" || state.status === "INVALID") ? (
        <Button component={Link} href={`/${locale}/resend-activation`} variant="outlined">
          {messages.activationResendCta}
        </Button>
      ) : null}
    </Stack>
  );
}
