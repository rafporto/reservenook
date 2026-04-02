# Phase 7 Test Scenarios

## Purpose

This document defines the implementation and verification baseline for Phase 7: Embedded Widget.

## Test Layers

- backend unit tests for widget policy and origin validation
- backend integration tests for widget bootstrap, booking reuse, and tenant enforcement
- frontend and runtime tests for widget initialization and rendering behavior
- end-to-end tests for embedded booking on an allowed host
- security tests for allowed-origin enforcement, clickjacking resistance, and abuse protection

## UC-57 Configure Embedded Widget Settings

### Security

- invalid allowed domains are rejected
- non-admin or stale-auth writes are denied

### Implemented Coverage

- backend company-configuration tests cover widget-settings validation, CSRF, role enforcement, and tenant isolation
- frontend backoffice tests verify widget settings remain visible in the company dashboard and widget usage telemetry is rendered only from the current tenant payload

## UC-58 Initialize Tenant Widget

### Security

- disallowed origins cannot initialize the widget
- bootstrap payload excludes secrets and sensitive internal configuration

### Implemented Coverage

- backend integration tests in `WidgetControllerTest` verify allowed-origin bootstrap success and disallowed-origin rejection
- frontend route tests verify the widget loader script generates a bootstrap-based iframe flow rather than exposing internal configuration directly
- frontend embedded-widget tests verify the iframe route fails closed when the widget bootstrap token is missing

## UC-59 Execute Embedded Booking Flow

### Security

- embedded flow enforces the same throttling, validation, and tenant checks as hosted booking
- CORS and framing behavior are explicitly verified

### Implemented Coverage

- backend integration tests verify embedded booking reuses the shared booking endpoints and records widget usage only after successful booking submission
- public booking web tests verify the embedded widget path can load booking configuration and complete the booking flows
- Next.js header tests verify only `/widget/:path*` is embeddable while the rest of the app keeps the non-framing baseline

## UC-60 Monitor Widget Usage

### Security

- tenant analytics access is isolated
- exported or displayed telemetry follows privacy policy

### Implemented Coverage

- backend company-backoffice response tests verify widget usage is returned only for the authenticated tenant
- frontend company-backoffice tests verify widget usage counts and recent origins are rendered from the tenant-scoped dashboard payload

## Verification Baseline

- `npm run test:web`
- `npm run lint:web`
- `npm run build:web`
- `.\gradlew.bat :apps:api:test`
