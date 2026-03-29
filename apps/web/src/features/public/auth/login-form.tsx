"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import Link from "next/link";
import { Alert, Button, Stack, TextField, Typography } from "@mui/material";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type LoginFormProps = {
  locale: SupportedLocale;
};

type LoginFormValues = {
  email: string;
  password: string;
};

type LoginErrorCode = "INVALID_CREDENTIALS" | "ACTIVATION_REQUIRED" | "INACTIVE_COMPANY";

function buildSchema(locale: SupportedLocale) {
  const messages = getPublicMessages(locale);

  return z.object({
    email: z.string().trim().min(1, messages.requiredField).email(messages.invalidEmail),
    password: z.string().min(8, messages.passwordTooShort)
  });
}

export function LoginForm({ locale }: LoginFormProps) {
  const messages = getPublicMessages(locale);
  const router = useRouter();
  const [errorState, setErrorState] = useState<{ message: string; code?: LoginErrorCode } | null>(null);
  const [successHint, setSuccessHint] = useState<string | null>(null);

  const form = useForm<LoginFormValues>({
    resolver: zodResolver(buildSchema(locale)),
    defaultValues: {
      email: "",
      password: ""
    }
  });

  async function onSubmit(values: LoginFormValues) {
    setErrorState(null);
    setSuccessHint(null);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/auth/login`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify(values)
      }
    );

    const payload = (await response.json().catch(() => null)) as {
      redirectTo?: string;
      message?: string;
      code?: LoginErrorCode;
    } | null;

    if (!response.ok) {
      setErrorState({
        message: payload?.message ?? messages.loginInvalidCredentials,
        code: payload?.code
      });
      return;
    }

    const redirectTo = payload?.redirectTo ?? "/";
    setSuccessHint(
      redirectTo.startsWith("/platform-admin") ? messages.loginNextStepPlatform : messages.loginNextStepCompany
    );
    router.push(redirectTo);
  }

  const emailValue = form.watch("email");

  return (
    <Stack spacing={3} component="form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
      {successHint ? <Alert severity="success">{successHint}</Alert> : null}
      {errorState ? <Alert severity="error">{errorState.message}</Alert> : null}

      {errorState?.code === "ACTIVATION_REQUIRED" ? (
        <Typography variant="body2">
          <Link href={`/${locale}/resend-activation${emailValue ? `?email=${encodeURIComponent(emailValue)}` : ""}`}>
            {messages.loginResendActivationCta}
          </Link>
        </Typography>
      ) : null}

      <Controller
        control={form.control}
        name="email"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.loginEmailLabel}
            type="email"
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Controller
        control={form.control}
        name="password"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.loginPasswordLabel}
            type="password"
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Button type="submit" size="large" variant="contained" disabled={form.formState.isSubmitting}>
        {form.formState.isSubmitting ? messages.loginSubmitting : messages.loginSubmit}
      </Button>
    </Stack>
  );
}
