# Operations Runbook

## Purpose

This document captures the production-readiness baseline that sits beyond the roadmap use cases.

It focuses on:

- operational alert delivery
- performance smoke verification
- deployment-time configuration expectations

## Operational Alerts

ReserveNook can now deliver real operational alerts through the configured SMTP transport.

Alert delivery is controlled through backend environment variables:

- `OPERATIONS_ALERTS_ENABLED`
- `OPERATIONS_ALERTS_RECIPIENT_EMAIL`
- `OPERATIONS_ALERTS_FROM_EMAIL`
- `OPERATIONS_ALERTS_COOLDOWN_MINUTES`

The alert channel is intended for platform operators, not end users.

Current alert triggers:

- company deletion failures
- company inactivity notice failures
- company deletion warning failures
- repeated rate-limited spikes on login, password reset, activation resend, and public booking intake

Cooldown protection prevents repeated duplicate alerts for the same alert scope from flooding the operator mailbox.

## Platform Admin Visibility

The platform-admin operations summary now shows:

- operational alerting status
- the configured alert recipient
- recent security audit records

This allows operators to verify whether production alerting is actually configured, not just assume it is.

## Performance Smoke Check

A lightweight performance smoke script is available at:

- `scripts/performance/public-booking-smoke.mjs`

Run it from the repository root:

- `npm run perf:public-booking-smoke`

Required environment variable:

- `PERF_COMPANY_SLUG`

Optional environment variables:

- `PERF_BASE_URL`
- `PERF_REQUESTS`
- `PERF_MAX_P95_MS`

The smoke check currently verifies:

- `GET /api/public/ping`
- `GET /api/public/companies/{slug}/booking-intake-config`

The command fails if any request is non-successful or if the measured p95 latency exceeds the configured threshold.

## Recommended Release Checks

Before a production release or infrastructure change:

1. confirm SMTP alert variables are configured
2. confirm platform admin shows alerting as enabled
3. run the standard CI baseline
4. run `npm run perf:public-booking-smoke` against a staging environment with a real seeded company
5. review recent platform audit events for unexpected rate-limit spikes or lifecycle failures
