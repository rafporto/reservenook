"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Alert, Button, MenuItem, Stack, TextField } from "@mui/material";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { defaultLocaleForLanguage, type SupportedLocale } from "@/lib/i18n/locales";
import { getPublicMessages } from "@/lib/i18n/messages";

const businessTypes = ["APPOINTMENT", "CLASS", "RESTAURANT"] as const;
const planTypes = ["TRIAL", "PAID"] as const;

type RegistrationFormValues = {
  companyName: string;
  businessType: (typeof businessTypes)[number];
  slug: string;
  email: string;
  password: string;
  planType: (typeof planTypes)[number];
  defaultLanguage: SupportedLocale;
  defaultLocale: string;
};

type RegistrationFormProps = {
  locale: SupportedLocale;
};

function buildSchema(locale: SupportedLocale) {
  const messages = getPublicMessages(locale);

  return z.object({
    companyName: z.string().trim().min(1, messages.requiredField),
    businessType: z.enum(businessTypes),
    slug: z
      .string()
      .trim()
      .min(1, messages.requiredField)
      .regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/, messages.invalidSlug),
    email: z.string().trim().min(1, messages.requiredField).email(messages.invalidEmail),
    password: z.string().min(8, messages.passwordTooShort),
    planType: z.enum(planTypes),
    defaultLanguage: z.enum(["en", "de", "pt"]),
    defaultLocale: z.string().trim().min(1, messages.requiredField)
  });
}

export function RegistrationForm({ locale }: RegistrationFormProps) {
  const messages = getPublicMessages(locale);
  const [serverError, setServerError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const form = useForm<RegistrationFormValues>({
    resolver: zodResolver(buildSchema(locale)),
    defaultValues: {
      companyName: "",
      businessType: "APPOINTMENT",
      slug: "",
      email: "",
      password: "",
      planType: "TRIAL",
      defaultLanguage: locale,
      defaultLocale: defaultLocaleForLanguage(locale)
    }
  });

  async function onSubmit(values: RegistrationFormValues) {
    setServerError(null);
    setSuccessMessage(null);

    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/public/companies/registration`,
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

    form.reset({
      companyName: "",
      businessType: "APPOINTMENT",
      slug: "",
      email: "",
      password: "",
      planType: "TRIAL",
      defaultLanguage: locale,
      defaultLocale: defaultLocaleForLanguage(locale)
    });
    setSuccessMessage(payload?.message ?? messages.successMessage);
  }

  return (
    <Stack spacing={3} component="form" noValidate onSubmit={form.handleSubmit(onSubmit)}>
      {successMessage ? <Alert severity="success">{successMessage}</Alert> : null}
      {serverError ? <Alert severity="error">{serverError}</Alert> : null}

      <Controller
        control={form.control}
        name="companyName"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.formCompanyName}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Controller
        control={form.control}
        name="businessType"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            select
            label={messages.formBusinessType}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          >
            <MenuItem value="APPOINTMENT">{messages.businessTypeAppointment}</MenuItem>
            <MenuItem value="CLASS">{messages.businessTypeClass}</MenuItem>
            <MenuItem value="RESTAURANT">{messages.businessTypeRestaurant}</MenuItem>
          </TextField>
        )}
      />

      <Controller
        control={form.control}
        name="slug"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.formSlug}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Controller
        control={form.control}
        name="email"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.formEmail}
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
            label={messages.formPassword}
            type="password"
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          />
        )}
      />

      <Controller
        control={form.control}
        name="planType"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            select
            label={messages.formPlanType}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
          >
            <MenuItem value="TRIAL">{messages.planTrial}</MenuItem>
            <MenuItem value="PAID">{messages.planPaid}</MenuItem>
          </TextField>
        )}
      />

      <Controller
        control={form.control}
        name="defaultLanguage"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            select
            label={messages.formDefaultLanguage}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
            onChange={(event) => {
              const nextLanguage = event.target.value as SupportedLocale;
              field.onChange(nextLanguage);
              form.setValue("defaultLocale", defaultLocaleForLanguage(nextLanguage), {
                shouldDirty: true
              });
            }}
          >
            <MenuItem value="en">English</MenuItem>
            <MenuItem value="de">Deutsch</MenuItem>
            <MenuItem value="pt">Português</MenuItem>
          </TextField>
        )}
      />

      <Controller
        control={form.control}
        name="defaultLocale"
        render={({ field, fieldState }) => (
          <TextField
            {...field}
            label={messages.formDefaultLocale}
            error={fieldState.invalid}
            helperText={fieldState.error?.message ?? " "}
            fullWidth
            slotProps={{
              input: {
                readOnly: true
              }
            }}
          />
        )}
      />

      <Button type="submit" size="large" variant="contained" disabled={form.formState.isSubmitting}>
        {form.formState.isSubmitting ? messages.submittingLabel : messages.submitLabel}
      </Button>
    </Stack>
  );
}
