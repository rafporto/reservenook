import { NextResponse } from "next/server";

export function GET() {
  const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  const script = `
(function () {
  var currentScript = document.currentScript;
  if (!currentScript) return;
  var slug = currentScript.getAttribute("data-company");
  if (!slug) return;
  var locale = currentScript.getAttribute("data-locale") || "en";
  var targetId = currentScript.getAttribute("data-target");
  var height = currentScript.getAttribute("data-height") || "760";
  var target = targetId ? document.getElementById(targetId) : null;
  if (!target) {
    target = document.createElement("div");
    currentScript.parentNode.insertBefore(target, currentScript.nextSibling);
  }
  fetch("${apiBaseUrl}/api/public/widget/" + encodeURIComponent(slug) + "/bootstrap?locale=" + encodeURIComponent(locale), {
    credentials: "omit",
    headers: { "Accept": "application/json" }
  }).then(function (response) {
    if (!response.ok) throw new Error("Widget bootstrap failed.");
    return response.json();
  }).then(function (payload) {
    var iframe = document.createElement("iframe");
    iframe.src = payload.iframeUrl;
    iframe.title = payload.companyName + " booking widget";
    iframe.loading = "lazy";
    iframe.referrerPolicy = "strict-origin-when-cross-origin";
    iframe.style.width = "100%";
    iframe.style.minHeight = height + "px";
    iframe.style.border = "0";
    iframe.style.borderRadius = "16px";
    iframe.style.background = "transparent";
    target.innerHTML = "";
    target.appendChild(iframe);
  }).catch(function () {
    target.innerHTML = '<div style="font-family: Georgia, serif; border: 1px solid rgba(83,58,43,0.16); border-radius: 16px; padding: 16px; color: #241A16; background: #FFF9F1;">ReserveNook widget could not be initialized for this origin.</div>';
  });
}());
`.trim();

  return new NextResponse(script, {
    headers: {
      "Content-Type": "application/javascript; charset=utf-8",
      "Cache-Control": "public, max-age=300",
      "Access-Control-Allow-Origin": "*",
      "Vary": "Origin"
    }
  });
}
