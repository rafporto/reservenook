"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Alert, Button, Stack, TextField } from "@mui/material";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type ResetPasswordFormProps = {
  locale: SupportedLocale;
  token?: string;
};

type ResetPasswordValues = {
  password: string;
};

function buildSchema(locale: SupportedLocale) {
  const messages = getPublicMessages(locale);

  return z.object({
    password: z.string().min(8, messages.passwordTooShort)
  });
}

export function ResetPasswordForm({ locale, token }: ResetPasswordFormProps) {
  const messages = getPublicMessages(locale);
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const form = useForm<ResetPasswordValues>({
    resolver: zodResolver(buildSchema(locale)),
    defaultValues: {
      password: ""
    }
  });

  async function onSubmit(values: ResetPasswordValues) {
    if (!token) {
      setServerError(messages.resetPasswordMissingToken);
      return;
    }

    setServerError(null);
    setSuccessMessage(null);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/auth/reset-password`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          token,
          password: values.password
        })
      }
    );

    const payload = (await response.json().catch(() => null)) as
      | { message?: string; redirectTo?: string; code?: string }
      | null;

    if (!response.ok) {
      if (payload?.code === "EXPIRED_TOKEN") {
        setServerError(payload.message ?? messages.resetPasswordExpiredToken);
        return;
      }

      if (payload?.code === "INVALID_TOKEN") {
        setServerError(payload.message ?? messages.resetPasswordInvalidToken);
        return;
      }

      setServerError(payload?.message ?? messages.genericError);
      return;
    }

    form.reset();
    setSuccessMessage(payload?.message ?? messages.resetPasswordSuccess);
    router.push(payload?.redirectTo ?? `/${locale}/login`);
  }

  return (
    <Stack spacing={3} component="form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
      {!token ? <Alert severity="error">{messages.resetPasswordMissingToken}</Alert> : null}
      {successMessage ? <Alert severity="success">{successMessage}</Alert> : null}
      {serverError ? <Alert severity="error">{serverError}</Alert> : null}

      <Controller
        control={form.control}
        name="password"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            type="password"
            label={messages.resetPasswordPasswordLabel}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Button
        type="submit"
        size="large"
        variant="contained"
        disabled={form.formState.isSubmitting || !token}
      >
        {form.formState.isSubmitting ? messages.resetPasswordSubmitting : messages.resetPasswordSubmit}
      </Button>

      <Button component={Link} href={`/${locale}/forgot-password`} variant="text">
        {messages.loginForgotPasswordCta}
      </Button>
    </Stack>
  );
}
