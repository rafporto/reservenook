"use client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class CsrfTokenError extends Error {
  constructor(
    message: string,
    readonly status?: number
  ) {
    super(message);
  }
}

export async function fetchCsrfToken() {
  const response = await fetch(`${API_BASE_URL}/api/auth/csrf-token`, {
    credentials: "include"
  });
  const payload = (await response.json().catch(() => null)) as { token?: string } | null;

  if (!response.ok || !payload?.token) {
    throw new CsrfTokenError("Could not load CSRF token.", response.status);
  }

  return payload.token;
}
