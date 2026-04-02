# Phase 8 Test Scenarios

## Purpose

This document defines the implementation and verification baseline for Phase 8: Hardening and Operational Maturity.

## Test Layers

- backend tests for retention, throttling policy, and audit durability
- frontend and UX tests for accessibility, localization, and operational admin views
- performance and load tests for availability-heavy flows
- security regression tests that consolidate the protections introduced in earlier phases

## UC-61 Expand Audit And Monitoring Coverage

### Tests

- audit records are created for all defined high-risk actions
- audit reads are tenant-safe and role-safe
- audit records cannot be modified through application flows

### Implemented Coverage

- backend platform-admin controller tests verify global operations-summary and security-audit reads
- backend company backoffice coverage verifies tenant security-audit data stays inside the authenticated company scope
- frontend platform-admin and company-backoffice tests verify the new operational and audit panels render the expected payload safely

## UC-62 Strengthen Abuse Prevention Controls

### Tests

- abuse thresholds can be configured safely
- public auth and booking throttles remain effective under burst traffic
- anomaly signals are captured without leaking account existence

### Implemented Coverage

- backend tests verify persisted abuse-policy updates through the platform-admin API
- `PublicRequestAbuseGuardTest` verifies login-specific, public-write, and public-read thresholds
- existing auth and public-booking tests continue to exercise the throttled paths under the configured policy

## UC-63 Manage Deletion And Retention Workflows

### Tests

- retention policies are enforced on schedule
- deletion workflows remain auditable
- deleted or retained data follows policy-specific access rules

### Implemented Coverage

- backend platform-admin controller tests verify legal-hold updates on tenant companies
- `CompanyDeletionServiceTest` verifies companies under legal hold are skipped by automated deletion
- lifecycle-job integration coverage remains in place for deletion and warning flows after the legal-hold rule was added

## UC-64 Validate Availability Performance At Scale

### Tests

- load tests cover appointment, class, restaurant, and widget booking surfaces
- expensive query patterns are capped or rejected safely
- performance regressions are visible in CI or pre-release gates where practical

### Implemented Coverage

- the existing public availability services keep server-side date and window caps in place
- the Phase 8 abuse-policy controls remain observable and configurable for high-traffic public read flows
- the CI baseline continues to exercise hosted and embedded booking availability in automated tests

## UC-65 Complete Accessibility And Localization Review

### Tests

- public and protected pages are validated for keyboard navigation, labels, contrast, and screen-reader cues
- each supported language preserves the intended flow and validation feedback
- security-critical pages remain excluded from indexing where appropriate

### Implemented Coverage

- the web app now derives the document `lang` from the selected route locale through middleware plus root-layout cookie handling
- existing public-flow tests continue to cover localized copy and validation behavior for the supported languages
- token-based routes and widget routes keep the non-indexing controls already established in the SEO baseline

## UC-66 Establish Operational Monitoring And Alerting

### Tests

- critical auth, booking, lifecycle, and abuse events emit expected signals
- alert conditions are documented and testable
- telemetry does not expose secrets or over-broad personal data

### Implemented Coverage

- platform-admin operations-summary exposes recent auth, booking, lifecycle, and rate-limit counters derived from durable audit events
- company backoffice now includes a tenant-safe security summary and recent security-audit feed
- widget and booking telemetry remain tenant-scoped and avoid exposing secrets in UI payloads

## Verification Baseline

- `npm run test:web`
- `npm run lint:web`
- `npm run build:web`
- `.\gradlew.bat :apps:api:test`
