"use client";

import { CsrfTokenError, fetchCsrfToken } from "@/lib/security/csrf";
import { ApiMessageResponse, CompanyBackofficeData } from "@/features/app/company/company-backoffice-types";

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export type CompanyBackofficeLoadResult =
  | { status: "loaded"; data: CompanyBackofficeData }
  | { status: "unauthorized" }
  | { status: "forbidden" }
  | { status: "error"; message: string };

export type CompanyBackofficeSaveResult<TResponse extends ApiMessageResponse> =
  | { status: "saved"; payload: TResponse }
  | { status: "unauthorized" }
  | { status: "forbidden" }
  | { status: "error"; message: string };

export async function loadCompanyBackoffice(slug: string): Promise<CompanyBackofficeLoadResult> {
  try {
    const response = await fetch(`${apiBaseUrl}/api/app/company/${slug}/backoffice`, {
      credentials: "include"
    });
    if (response.status === 401) {
      return { status: "unauthorized" };
    }
    if (response.status === 403) {
      return { status: "forbidden" };
    }
    if (!response.ok) {
      return { status: "error", message: "The company backoffice could not be loaded." };
    }

    return {
      status: "loaded",
      data: (await response.json()) as CompanyBackofficeData
    };
  } catch {
    return { status: "error", message: "The company backoffice could not be loaded." };
  }
}

export async function saveCompanyBackofficeSection<TResponse extends ApiMessageResponse>(
  endpoint: string,
  method: "PUT" | "POST",
  body: Record<string, unknown>
): Promise<CompanyBackofficeSaveResult<TResponse>> {
  try {
    const csrfToken = await fetchCsrfToken();
    const response = await fetch(`${apiBaseUrl}${endpoint}`, {
      method,
      credentials: "include",
      headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": csrfToken },
      body: JSON.stringify(body)
    });
    const payload = (await response.json().catch(() => null)) as TResponse;

    if (response.status === 401) {
      return { status: "unauthorized" };
    }
    if (response.status === 403) {
      return { status: "forbidden" };
    }
    if (!response.ok || payload == null) {
      return { status: "error", message: payload?.message ?? "The changes could not be saved." };
    }

    return { status: "saved", payload };
  } catch (error) {
    if (error instanceof CsrfTokenError && error.status === 401) {
      return { status: "unauthorized" };
    }

    return { status: "error", message: "The changes could not be saved." };
  }
}
