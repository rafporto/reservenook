"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Alert, Button, Stack, TextField } from "@mui/material";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import type { SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

type ForgotPasswordFormProps = {
  locale: SupportedLocale;
};

type ForgotPasswordValues = {
  email: string;
};

function buildSchema(locale: SupportedLocale) {
  const messages = getPublicMessages(locale);

  return z.object({
    email: z.string().trim().min(1, messages.requiredField).email(messages.invalidEmail)
  });
}

export function ForgotPasswordForm({ locale }: ForgotPasswordFormProps) {
  const messages = getPublicMessages(locale);
  const [serverError, setServerError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const form = useForm<ForgotPasswordValues>({
    resolver: zodResolver(buildSchema(locale)),
    defaultValues: {
      email: ""
    }
  });

  async function onSubmit(values: ForgotPasswordValues) {
    setServerError(null);
    setSuccessMessage(null);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/auth/forgot-password`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(values)
      }
    );

    const payload = (await response.json().catch(() => null)) as { message?: string } | null;

    if (!response.ok) {
      setServerError(payload?.message ?? messages.genericError);
      return;
    }

    form.reset();
    setSuccessMessage(payload?.message ?? messages.forgotPasswordNeutralSuccess);
  }

  return (
    <Stack spacing={3} component="form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
      {successMessage ? <Alert severity="success">{successMessage}</Alert> : null}
      {serverError ? <Alert severity="error">{serverError}</Alert> : null}

      <Controller
        control={form.control}
        name="email"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            type="email"
            label={messages.forgotPasswordEmailLabel}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Button type="submit" size="large" variant="contained" disabled={form.formState.isSubmitting}>
        {form.formState.isSubmitting ? messages.forgotPasswordSubmitting : messages.forgotPasswordSubmit}
      </Button>
    </Stack>
  );
}
