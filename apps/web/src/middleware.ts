import { NextResponse, type NextRequest } from "next/server";

const supportedLocales = new Set(["en", "de", "pt"]);

export function middleware(request: NextRequest) {
  const response = NextResponse.next();
  const locale = request.nextUrl.pathname.split("/").filter(Boolean)[0];

  if (supportedLocales.has(locale)) {
    response.cookies.set("rn-locale", locale, { path: "/" });
  }

  return response;
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"]
};
