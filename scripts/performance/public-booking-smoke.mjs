const baseUrl = (process.env.PERF_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");
const companySlug = process.env.PERF_COMPANY_SLUG;
const requestCount = Number(process.env.PERF_REQUESTS ?? "20");
const maxP95Ms = Number(process.env.PERF_MAX_P95_MS ?? "750");

if (!companySlug) {
  console.error("PERF_COMPANY_SLUG is required.");
  process.exit(1);
}

const targets = [
  { name: "public-ping", url: `${baseUrl}/api/public/ping` },
  { name: "booking-config", url: `${baseUrl}/api/public/companies/${companySlug}/booking-intake-config` }
];

async function measure(url) {
  const startedAt = performance.now();
  const response = await fetch(url, { headers: { Accept: "application/json" } });
  const durationMs = performance.now() - startedAt;
  return {
    ok: response.ok,
    status: response.status,
    durationMs
  };
}

function percentile(values, percentileRank) {
  const sorted = [...values].sort((left, right) => left - right);
  const index = Math.min(sorted.length - 1, Math.ceil((percentileRank / 100) * sorted.length) - 1);
  return sorted[index];
}

async function runTarget(target) {
  const durations = [];
  for (let index = 0; index < requestCount; index += 1) {
    const result = await measure(target.url);
    if (!result.ok) {
      throw new Error(`${target.name} failed with HTTP ${result.status}.`);
    }
    durations.push(result.durationMs);
  }

  const averageMs = durations.reduce((sum, value) => sum + value, 0) / durations.length;
  const p95Ms = percentile(durations, 95);
  return {
    ...target,
    averageMs,
    p95Ms
  };
}

async function main() {
  console.log(`Running performance smoke against ${baseUrl} for company ${companySlug}`);
  const results = [];

  for (const target of targets) {
    const result = await runTarget(target);
    results.push(result);
    console.log(`${result.name}: avg=${result.averageMs.toFixed(1)}ms p95=${result.p95Ms.toFixed(1)}ms`);
  }

  const failures = results.filter((result) => result.p95Ms > maxP95Ms);
  if (failures.length > 0) {
    console.error(`Performance smoke failed. p95 threshold ${maxP95Ms}ms exceeded for: ${failures.map((result) => result.name).join(", ")}`);
    process.exit(1);
  }

  console.log(`Performance smoke passed. Max allowed p95=${maxP95Ms}ms`);
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : String(error));
  process.exit(1);
});
