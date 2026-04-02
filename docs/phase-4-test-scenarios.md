# Phase 4 Test Scenarios

## Purpose

This document defines the implementation-ready test baseline for Phase 4: Appointment Module.

## Test Layers

- backend unit tests for service rules, provider rules, slot generation, and booking transitions
- backend integration tests for persistence, scheduling constraints, and endpoint authorization
- frontend tests for service management, provider management, availability views, and booking forms
- end-to-end tests for the key appointment booking journey
- security tests for public abuse protection, slot replay, tenant isolation, and provider-scope authorization

## Implemented Baseline

- `AppointmentControllerTest` covers admin management of appointment services, provider linking, provider availability updates, and provider self-schedule reads
- `PublicBookingControllerTest` covers public appointment availability, public appointment booking creation, and the existing public booking-intake abuse throttling path
- `public-booking-page.test.tsx` covers the public appointment selection flow from service selection through slot booking
- `company-backoffice-screen.test.tsx` covers the Phase 4 admin surface presence for appointment services and provider availability controls

## UC-35 Manage Appointment Services

### Functional

- valid service definitions persist
- invalid durations, buffers, or status values are rejected

### Security

- non-admin writes are denied
- service updates require CSRF and recent authentication
- audit events are recorded

## UC-36 Manage Providers

### Functional

- provider records can be created and updated
- provider-to-user linking follows tenant rules

### Security

- cross-tenant linking is denied
- guessed provider identifiers from another tenant are rejected

## UC-37 Configure Provider Availability

### Functional

- valid weekly rules and exceptions are accepted
- invalid overlaps are rejected

### Security

- unauthorized provider availability updates are denied
- audit records exist for provider schedule changes

## UC-38 Generate Appointment Slots

### Functional

- slot generation respects service duration, buffers, closures, and provider hours
- unavailable times are excluded

### Security

- extreme query windows are rejected or capped
- tenant scoping is enforced for internal slot-generation APIs

## UC-39 View Public Appointment Availability

### Functional

- public availability returns valid slots for enabled services
- disabled services are hidden

### Security

- repeated availability scraping is rate-limited
- inactive-tenant responses are safe
- internal provider constraints are not overexposed

## UC-40 Book Appointment

### Functional

- valid appointment booking creates the expected booking record
- invalid or stale slot requests are rejected

### End-to-End

- customer books an appointment and receives the expected confirmation or pending state

### Security

- booking replay and duplicate-submission behavior is safe
- abusive repeated public bookings are throttled
- malformed public inputs are rejected server-side

## UC-41 Confirm Or Reject Appointment

### Functional

- manual confirmation and rejection update appointment state correctly

### Security

- unauthorized actors cannot confirm or reject
- duplicate confirmation actions are rejected or idempotent
- audit trail captures actor and outcome

## UC-42 Provider Views Own Schedule

### Functional

- provider-linked user sees only their own schedule

### Security

- provider cannot enumerate another provider's bookings
- customer data exposure follows least-privilege policy
