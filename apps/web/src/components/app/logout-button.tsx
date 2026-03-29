"use client";

import { Button } from "@mui/material";
import { useRouter } from "next/navigation";

export function LogoutButton() {
  const router = useRouter();

  async function handleLogout() {
    const response = await fetch(
      `${process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"}/api/auth/logout`,
      {
        method: "POST",
        credentials: "include"
      }
    );

    const payload = (await response.json().catch(() => null)) as { redirectTo?: string } | null;
    router.push(payload?.redirectTo ?? "/en/login");
  }

  return (
    <Button variant="outlined" color="primary" onClick={handleLogout}>
      Logout
    </Button>
  );
}
